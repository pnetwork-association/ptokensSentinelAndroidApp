package com.ptokenssentinelandroidapp.database;

import static com.ptokenssentinelandroidapp.database.Operations.deleteFile;

import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;
import android.util.Base64;
import android.util.Pair;

import org.apache.commons.codec.binary.Hex;
import org.sqlite.database.sqlite.SQLiteDatabase;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import com.ptokenssentinelandroidapp.rustlogger.RustLogger;
import com.ptokenssentinelandroidapp.strongbox.Strongbox;

// NOTE: At the end of every database transaction, the db is hashed and that hash signed by the TEE
// protected key. When a transaction is started, the database is again hashed, and checked against
// the previous, signed hash, in order to verify that the database hasn't been tampered with. This
// enum holds the various stats this db can be in, based on this integrity check.
enum DbIntegrity {
    VALID,
    INVALID,
    NO_HASH,
    NO_STATE,
    FIRST_RUN,
    UNVERIFIABLE,
    HASH_WRITTEN,
    NO_SIGNATURE,
}

public class DatabaseWiring implements DatabaseInterface {
    public static final String CLASS_NAME = "Java" + DatabaseWiring.class.getName();
    private static final String NAME_SIGNED_STATE_HASH = "state-hash.sig";
    private static final String SUCCESS_MESSAGE = "success";

    private boolean START_DB_TX_IN_PROGRESS = false;
    private boolean END_DB_TX_IN_PROGRESS = false;
    private boolean DB_TX_IN_PROGRESS = false;

    private Context context;
    private SQLiteDatabase db;
    private Map<String, byte[]> cache;
    private List<String> removedKeys;
    private boolean verifyDbIntegrityEnabled;
    private boolean writeSignedStateHashEnabled;
    private boolean strongboxEnabled;

    private String createJsonRpcResponse(Long id, String body) {
        return "{\"jsonrpc\": \"2.0\", \"id\": " + id + ", " + body + "}";

    }

    private String createSuccessJsonResponse(Long id, String msg) {
        return createJsonRpcResponse(id, "\"result\": " + msg);
    }

    private String createErrorJsonResponse(Long id, Long errorCode, String msg) {
        return createJsonRpcResponse(id, "\"error\": {\"code\": " + errorCode + ", \"message\" :" + msg + "}");
    }

    public DatabaseWiring(
        Context context,
        SQLiteDatabase db,
        boolean verifyStateHash
    ) {
        this.db = db;
        this.context = context;
        this.removedKeys = new ArrayList<>();
        this.cache = new ConcurrentHashMap<>();
        this.verifyDbIntegrityEnabled = verifyStateHash;
    }

    public DatabaseWiring(
        Context context,
        SQLiteDatabase db,
        boolean verifyStateHash,
        boolean writeSignedStateHash
    ) {
        this(context, db, verifyStateHash);
        this.writeSignedStateHashEnabled = writeSignedStateHash;
    }

    public DatabaseWiring(
        Context context,
        SQLiteDatabase db,
        boolean verifyStateHash,
        boolean writeSignedStateHash,
        boolean strongboxEnabled
    ) {
        this(context, db, verifyStateHash, writeSignedStateHash);
        this.strongboxEnabled = strongboxEnabled;
    }


    @Override
    public void put(byte[] key, byte[] value, byte dataSensitivity) {
        String hexKey = new String(Hex.encodeHex(key));

        removedKeys.remove(hexKey);

        if (dataSensitivity == (byte) 255) {
            cache.put(hexKey, Strongbox.encrypt(value));
        } else {
            cache.put(hexKey, value);
        }

        RustLogger.rustLog(CLASS_NAME + " put: key " + hexKey + " inserted in the cache");
    }

    @Override
    public byte[] get(byte[] key, byte dataSensitivity) throws Exception {
        String hexKey = new String(Hex.encodeHex(key));

        try {
            if (removedKeys.contains(hexKey)) {
                RustLogger.rustLog(CLASS_NAME + " get: value for " + hexKey + " was removed");
                throw new Exception("get: key " + hexKey + " not found");
            }

            if (dataSensitivity == (byte) 255) {
                try {
                    return Strongbox.decrypt(readCache(hexKey));
                } catch (NullPointerException e) {
                    return Strongbox.decrypt(readDatabase(hexKey));
                }
            } else {
                try {
                    return readCache(hexKey);
                } catch (NullPointerException e) {
                    return readDatabase(hexKey);
                }
            }
        } catch (CursorIndexOutOfBoundsException e) {
            throw new Exception("get: key " + hexKey + " not stored inside the db");
        }
    }

    @Override
    public void delete(byte[] key) {
        String hexKey = new String(Hex.encodeHex(key));

        removedKeys.add(hexKey);
    }

    // NOTE: This returns a jsonrpc spec response so that the rust library which calls it can glean
    // more information about any failure states this function ends up in. Previously, exceptions
    // were use, but the JNI interop between rust and java doesn't allow us access to any
    // information _about_ an exception, just that one occurred. All JNI can then do is tell the
    // java run time to print the exception, and then clear it ready for passing control back to
    // java. However this means the rust library has no idea what specifically occurred. And so
    // instead we return a json encoded string for the rust library to parse in order to better
    // understand and act on any errors that might occur.
    @Override
    public String startTransaction() {
        long rpcId = 1;
        RustLogger.rustLog(CLASS_NAME + " starting db tx...");
        String successResponse = createSuccessJsonResponse(rpcId, SUCCESS_MESSAGE);

        if (START_DB_TX_IN_PROGRESS) {
            RustLogger.rustLog(CLASS_NAME + " cannot start db tx, one is already starting");
            return successResponse;
        } else if (DB_TX_IN_PROGRESS) {
            RustLogger.rustLog(CLASS_NAME + " cannot start db tx, one is already in progress");
            return successResponse;
        }

        START_DB_TX_IN_PROGRESS = true;
        String skipMessage = "signed state hash verification skipped";

        if (!verifyDbIntegrityEnabled) {
            RustLogger.rustLog(CLASS_NAME + " " + skipMessage);
        } else {
            DbIntegrity dbIntegrity = getDbIntegrity();

            if (dbIntegrity == DbIntegrity.FIRST_RUN) {
                RustLogger.rustLog(CLASS_NAME + skipMessage + " due to first run");
            } else {
                String errorMessage = "{\"msg\": \"signed state hash verification failed\", \"dbIntegrity\": \"" + dbIntegrity.name() + "\"}";
                long errorCode = dbIntegrity.ordinal();
                RustLogger.rustLog(CLASS_NAME + errorMessage);
                START_DB_TX_IN_PROGRESS = false;
                return createErrorJsonResponse(rpcId, errorCode, errorMessage);
            }
        }

        db.beginTransaction();
        START_DB_TX_IN_PROGRESS = false;
        RustLogger.rustLog(CLASS_NAME + " db tx started");
        DB_TX_IN_PROGRESS = true;
        
        return successResponse;
    }

    public void clearCaches() {
        RustLogger.rustLog(CLASS_NAME + " clearing caches...");
        this.cache.clear();
        this.removedKeys = new ArrayList<>();
        RustLogger.rustLog(CLASS_NAME + " caches cleared");
    }

    public void cancelTransaction() throws DatabaseException {
        RustLogger.rustLog(CLASS_NAME + " cancelling db tx...");

        if (END_DB_TX_IN_PROGRESS || START_DB_TX_IN_PROGRESS) {
            if (END_DB_TX_IN_PROGRESS) {
                RustLogger.rustLog(CLASS_NAME + " cannot cancel db tx, ending tx is in progress");
            } else {
                RustLogger.rustLog(CLASS_NAME + " cannot cancel db tx, starting tx is in progress");
            }
            throw new DatabaseException("cancelling db tx failed");
        }

        DB_TX_IN_PROGRESS = false;
        END_DB_TX_IN_PROGRESS = false;
        START_DB_TX_IN_PROGRESS = false;

        if (db.inTransaction()) {
            // NOTE: Ending the tx without marking it successful is how we roll it back.
            db.endTransaction();
            this.clearCaches();
            RustLogger.rustLog(CLASS_NAME + " db tx cancelled");
        } else {
            RustLogger.rustLog(CLASS_NAME + " no db tx in progress to cancel");
        }
    }

    // NOTE: See note for `startTransaction`.
    @Override
    public String endTransaction() {
        RustLogger.rustLog(CLASS_NAME + " ending db tx...");
        long rpcId = 2;
        String successResponse = createSuccessJsonResponse(rpcId, SUCCESS_MESSAGE);

        try {
            if (END_DB_TX_IN_PROGRESS) {
                RustLogger.rustLog(CLASS_NAME + " end db tx already in progress");
                return successResponse;
            } 

            if (!DB_TX_IN_PROGRESS) {
                RustLogger.rustLog(CLASS_NAME + " no db tx in progress to end");
                return successResponse;
            } 

            if (START_DB_TX_IN_PROGRESS) {
                long errorCode = 42; // FIXME magic number
                String errorMessage = "cannot end db tx, one is currently starting";
                RustLogger.rustLog(CLASS_NAME + " " + errorMessage);
                return createErrorJsonResponse(rpcId, errorCode, errorMessage);
            } else {
                START_DB_TX_IN_PROGRESS = false;
            }

            END_DB_TX_IN_PROGRESS = true;

            RustLogger.rustLog(CLASS_NAME + " writing keys: ");

            for (String key : cache.keySet()) {
                SQLiteHelper.insertOrReplace(db, key, cache.get(key));
            }

            for (String key : removedKeys) {
                SQLiteHelper.deleteKey(db, key);
            }

            db.setTransactionSuccessful();
            this.clearCaches();
        } finally {
            db.endTransaction();
            DB_TX_IN_PROGRESS = false;
            END_DB_TX_IN_PROGRESS = false;
            START_DB_TX_IN_PROGRESS = false;
            RustLogger.rustLog(CLASS_NAME + " keys written, db tx ended successfully");
        }

        if (!writeSignedStateHashEnabled) {
            RustLogger.rustLog(CLASS_NAME + " skipping state hash writing...");
            return successResponse;
        } 

        var dbIntegrity = writeSignedStateHash();
        if (dbIntegrity == DbIntegrity.HASH_WRITTEN) {
            return successResponse;
        } else {
            long errorCode = dbIntegrity.ordinal();
            String errorMessage = "{\"msg\": \"failed to write signed state hash\", \"dbIntegrity\": \"" + dbIntegrity.name() + "\"}";
            RustLogger.rustLog(CLASS_NAME + " " + errorMessage);
            return createErrorJsonResponse(rpcId, errorCode, errorMessage);
        }
    }


    private DbIntegrity writeSignedStateHash() {
        byte[] hash = getCurrentStateHash();

        if (hash == null) {
            RustLogger.rustLog(CLASS_NAME + " writing signed state failed, hash not found");
            return DbIntegrity.NO_HASH;
        }

        int aliasNumber = Strongbox.getLatestAliasNumber();

        String oldAlias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber;

        String newAlias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + (aliasNumber + 1);

        Strongbox.generateSigningKey(newAlias, this.strongboxEnabled);
        RustLogger.rustLog(CLASS_NAME + " switch key " + oldAlias + " <=> " + newAlias);
        byte[] signedState = Strongbox.sign(newAlias, hash);

        RustLogger.rustLog(CLASS_NAME + " new signed state hash " +
            Base64.encodeToString(signedState, Base64.DEFAULT)
        );

        Operations.writeBytes(context, NAME_SIGNED_STATE_HASH, signedState);

        Strongbox.removeKey(oldAlias);

        return DbIntegrity.HASH_WRITTEN;
    }

    private byte[] getCurrentStateHash() {
        try {
            ArrayList<Pair<String, byte[]>> keyValuePairs = SQLiteHelper.getKeysAndHashedValues(db);

            if (keyValuePairs.isEmpty()) {
                RustLogger.rustLog(CLASS_NAME + " no keys found!");
                return null;
            }

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

            for (Pair<String, byte[]> keyValue : keyValuePairs){
                String key = keyValue.first;
                sha1.update(key.getBytes());
                sha1.update(keyValue.second);
            }

            byte[] currentStateHash = sha1.digest();
            RustLogger.rustLog(CLASS_NAME + " current state hash"
                    + Base64.encodeToString(currentStateHash, Base64.DEFAULT)
            );

            return currentStateHash;
        } catch (NoSuchAlgorithmException e) {
            RustLogger.rustLog(CLASS_NAME," SHA-1 not supported: " + e.getMessage());
        } catch (Exception e) {
            RustLogger.rustLog(CLASS_NAME," current state hash error: " + e.getMessage());
        }

        return null;
    }


    private DbIntegrity getDbIntegrity() {
        RustLogger.rustLog(CLASS_NAME + "Verifying signed state hash...");

        byte[] signature = Operations.readBytes(context, NAME_SIGNED_STATE_HASH);
        byte[] hash = getCurrentStateHash();

        boolean signatureExists = (signature != null && signature.length > 0);
        boolean hashExists = (hash != null);

        if (!signatureExists && hashExists) {
            RustLogger.rustLog(CLASS_NAME + " no signature for existing state");
            return DbIntegrity.NO_SIGNATURE;
        } else if (signatureExists && !hashExists) {
            RustLogger.rustLog(CLASS_NAME + " no state for existing signature");
            return DbIntegrity.NO_STATE;
        } else if (!signatureExists) {
            RustLogger.rustLog(CLASS_NAME + " first run!");
            return DbIntegrity.FIRST_RUN;
        }

        int aliasNumber = Strongbox.getLatestAliasNumber();

        if (aliasNumber == -1) {
            RustLogger.rustLog(CLASS_NAME + "unverifiable signature for existing state - aborting");
            return  DbIntegrity.UNVERIFIABLE;
        }

        String alias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber;

        if (!Strongbox.verify(alias, hash, signature)) {
            RustLogger.rustLog(CLASS_NAME, "invalid signature for existing state");
            return  DbIntegrity.INVALID;
        } 

        RustLogger.rustLog(CLASS_NAME + " signed state hash verified");
        return  DbIntegrity.VALID;
      }

    @Override
    public void close() {
        SQLiteHelper helper = new SQLiteHelper(context);
        // NOTE: This will rollback any db transactions in progress also.
        db.close();
        helper.close();
        clearCaches();
        RustLogger.rustLog(CLASS_NAME + " db closed");
    }

    private byte[] readCache(String hexKey) {
        byte[] value = cache.get(hexKey);
        if (value == null)
            throw new NullPointerException("Value not found in the cache");
        return value;
    }

    private byte[] readDatabase(String hexKey) {
        byte[] value = SQLiteHelper.getBytesFromKey(db, hexKey);

        if (value == null)
            throw new NullPointerException("Value not found in the database");
        return value;
    }

    public void drop() {
        // FIXME gate this somehow (debug signature?)
        SQLiteHelper.drop(context);
        // NOTE: If we're dropping the db, we also need to drop the signature & hash since 
        // otherwise db state verification (if enabled) would fail.
        deleteFile(NAME_SIGNED_STATE_HASH);
    }
}
