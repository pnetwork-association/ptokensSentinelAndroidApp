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
import com.ptokenssentinelandroidapp.strongbox.StrongboxException;

public class DatabaseWiring implements DatabaseInterface {
    public static final String CLASS_NAME = "Java" + DatabaseWiring.class.getName();
    private static final String NAME_SIGNED_STATE_HASH = "state-hash.sig";

    private boolean START_DB_TX_IN_PROGRESS = false;
    private boolean END_DB_TX_IN_PROGRESS = false;
    private boolean DB_TX_IN_PROGRESS = false;

    private Context context;
    private SQLiteDatabase db;
    private Map<String, byte[]> cache;
    private List<String> removedKeys;
    private boolean verifySignedStateHashEnabled;
    private boolean writeSignedStateHashEnabled;
    private boolean strongboxEnabled;

    public DatabaseWiring(
        Context context,
        SQLiteDatabase db,
        boolean verifyStateHash
    ) {
        this.db = db;
        this.context = context;
        this.removedKeys = new ArrayList<>();
        this.cache = new ConcurrentHashMap<>();
        this.verifySignedStateHashEnabled = verifyStateHash;
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

    @Override
    public void startTransaction() throws DatabaseException {
        RustLogger.rustLog(CLASS_NAME + " starting db tx...");
        if (START_DB_TX_IN_PROGRESS) {
            RustLogger.rustLog(CLASS_NAME + " cannot start db tx, one is already starting");
            return;
        } else if (DB_TX_IN_PROGRESS) {
            RustLogger.rustLog(CLASS_NAME + " cannot start db tx, one is already in progress");
            return;
        }

        START_DB_TX_IN_PROGRESS = true;

        if (verifySignedStateHashEnabled) {
            try {
                verifySignedStateHash();
            } catch (StrongboxException e) {
                RustLogger.rustLog(CLASS_NAME + " signed state hash verification failed: " + e.getMessage());
                START_DB_TX_IN_PROGRESS = false;
                throw new DatabaseException("Start tx failed");
            }
        } else {
            RustLogger.rustLog(CLASS_NAME + " signed state hash verification skipped");
        }

        db.beginTransaction();
        START_DB_TX_IN_PROGRESS = false;
        RustLogger.rustLog(CLASS_NAME + " db tx started");
        DB_TX_IN_PROGRESS = true;
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

    @Override
    public void endTransaction() throws DatabaseException {
        RustLogger.rustLog(CLASS_NAME + " ending db tx...");
        try {
            if (END_DB_TX_IN_PROGRESS) {
                RustLogger.rustLog(CLASS_NAME + " end db tx already in progress");
                return;
            } if (!DB_TX_IN_PROGRESS) {
                RustLogger.rustLog(CLASS_NAME + " no db tx in progress to end");
                return;
            } if (START_DB_TX_IN_PROGRESS) {
                throw new DatabaseException("cannot end db tx, one is currently starting");
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

        try {
            if (writeSignedStateHashEnabled) {
                writeSignedStateHash();
            } else {
                RustLogger.rustLog(CLASS_NAME + " skipping state hash writing...");
            }
        } catch (DatabaseException e) {
            RustLogger.rustLog(CLASS_NAME + " failed to write the state hash: " + e.getMessage());
        }
    }


    private void writeSignedStateHash() throws DatabaseException {

        byte[] hash = getCurrentStateHash();
        if (hash == null) {
            throw new DatabaseException("Write signed state failed, hash not found");
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


    private void verifySignedStateHash() throws StrongboxException {

        byte[] signature = Operations.readBytes(context, NAME_SIGNED_STATE_HASH);
        byte[] hash = getCurrentStateHash();

        boolean signatureExists = (signature != null && signature.length > 0);
        boolean hashExists = (hash != null);
        if (!signatureExists && hashExists) {
            throw new StrongboxException("missing signature for existing state");
        } else if (signatureExists && !hashExists) {
            throw new StrongboxException("existing signature for missing state");
        } else if (!signatureExists) {
            RustLogger.rustLog(CLASS_NAME + " first run!");
            return;
        }

        // NOTE: We've reached this point meaning that we have a signature and
        // a state hash
        int aliasNumber = Strongbox.getLatestAliasNumber();
        if (aliasNumber == -1) {
            throw new StrongboxException("unverifiable signature for existing state - aborting");
        }
        String alias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber;
        if (!Strongbox.verify(alias, hash, signature)) {
            throw new StrongboxException("invalid signature for existing state");
        } else {
            RustLogger.rustLog(CLASS_NAME + " signed state hash verified");
        }
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
