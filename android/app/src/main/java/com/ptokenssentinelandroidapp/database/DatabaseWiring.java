package com.ptokenssentinelandroidapp.database;

import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;

import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.codec.binary.Hex;
import android.database.sqlite.SQLiteDatabase;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
import io.ptokens.security.Strongbox;
import io.ptokens.security.StrongboxException;
import io.ptokens.utils.Operations;
*/
/**
 * Implements the database interface to be exposed
 * to jni.
 */
public class DatabaseWiring implements DatabaseInterface {
    // FIXME rm!
    public static void callback() { 
        System.out.println("database wiring called From JNI");
    }


    public static final String TAG = DatabaseWiring.class.getName();
    private static final String NAME_SIGNED_STATE_HASH = "state-hash.sig";

    private boolean START_TX_IN_PROGRESS = false;
    private boolean END_TX_IN_PROGRESS = false;

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

        //SQLiteHelper.loadExtension(db);
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

        //if (dataSensitivity == (byte) 255) {
            //cache.put(hexKey, Strongbox.encrypt(value));
        //} else {
            cache.put(hexKey, value);
        //}

        Log.d(TAG, "put: key " + hexKey + " inserted in the cache");
    }

    @Override
    public byte[] get(byte[] key, byte dataSensitivity) throws Exception {
        String hexKey = new String(Hex.encodeHex(key));

        try {
            if (removedKeys.contains(hexKey)) {
                Log.d(TAG, "get: value for " + hexKey + " was removed");
                throw new Exception("get: key " + hexKey + " not found");
            /*
            } else if (dataSensitivity == (byte) 255) {
                try {
                    return Strongbox.decrypt(readCache(hexKey));
                } catch (NullPointerException e) {
                    return Strongbox.decrypt(readDatabase(hexKey));
                }
            */
            } else {
                try {
                    return readCache(hexKey);
                } catch (NullPointerException e) {
                    Log.d(TAG, "get: value for " + hexKey + " not in cache, checking db...");
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
        Log.i(TAG, "start transaction in progress!");
        if (START_TX_IN_PROGRESS) {
            return;
        }
        START_TX_IN_PROGRESS = true;

        /*
        if (verifySignedStateHashEnabled) {
            try {
                verifySignedStateHash();
            } catch (StrongboxException e) {
                Log.e(TAG, "signed state hash verification failed!", e);
                throw new DatabaseException("Start transaction failed");
            }
        } else {
        */
            Log.i(TAG, "signed state hash verification skipped");
        //}

        db.beginTransaction();
    }

    @Override
    public void endTransaction() throws DatabaseException {
        Log.i(TAG, "endTransaction in progress... ");

        try {
            if (END_TX_IN_PROGRESS) {
                return;
            } if (!START_TX_IN_PROGRESS) {
                throw new DatabaseException("invalid order, call startTransaction first!");
            } else {
                START_TX_IN_PROGRESS = false;
            }

            END_TX_IN_PROGRESS = true;

            Log.v(TAG, "writing keys: ");

            for (String key : cache.keySet()) {
                SQLiteHelper.insertOrReplace(db, key, cache.get(key));
            }

            for (String key : removedKeys) {
                SQLiteHelper.deleteKey(db, key);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        /*
        try {
            if (writeSignedStateHashEnabled) {
                writeSignedStateHash();    
            } else {
                Log.w(TAG, "skipping state hash writing...");                        
            }
        } catch (DatabaseException e) {
            Log.e(TAG, "failed to write the state hash", e);
        } finally {
            START_TX_IN_PROGRESS = false;
        }
        */
    }

    /*
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
        Log.i(TAG, "switch key " + oldAlias + " <=> " + newAlias);
        byte[] signedState = Strongbox.sign(newAlias, hash);

        Log.i(TAG, "new signed state hash " +
                Base64.encodeToString(signedState, Base64.DEFAULT)
        );

        Operations.writeBytes(context, NAME_SIGNED_STATE_HASH, signedState);

        Strongbox.removeKey(oldAlias);
    }
    */

    private byte[] getCurrentStateHash() {
        try {
            ArrayList<Pair<String, byte[]>> keyValuePairs = SQLiteHelper.getKeysAndHashedValues(db);

            if (keyValuePairs.isEmpty()) {
                Log.w(TAG, "no keys found!");
                return null;
            }

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

            for (Pair<String, byte[]> keyValue : keyValuePairs){
                String key = keyValue.first;
                sha1.update(key.getBytes());
                sha1.update(keyValue.second);
            }

            byte[] currentStateHash = sha1.digest();
            Log.i(TAG, "current state hash"
                    + Base64.encodeToString(currentStateHash, Base64.DEFAULT)
            );

            return currentStateHash;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG,"SHA-1 not supported", e);
        } catch (Exception e) {
            Log.e(TAG,"current state hash error:" + e.getMessage());
        }

        return null;
    }

    /*
    private void verifySignedStateHash() throws StrongboxException {

        byte[] signature = Operations.readBytes(context, NAME_SIGNED_STATE_HASH);
        byte[] hash = getCurrentStateHash();

        boolean signatureExists = (signature != null && signature.length > 0);
        boolean hashExists = (hash != null);
        if (!signatureExists && hashExists) {
            throw new StrongboxException("✘ Missing signature for existing state!");
        } else if (signatureExists && !hashExists) {
            throw new StrongboxException("✘ Existing signature for missing state!");
        } else if (!signatureExists) {
            Log.i(TAG, "first run!");
            return;
        }

        // Reached this point the we have a signature and
        // a state hash
        int aliasNumber = Strongbox.getLatestAliasNumber();
        if (aliasNumber == -1) {
            throw new StrongboxException("✘ Unverifiable signature for existing state! Aborting!");
        }
        String alias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber;
        if (!Strongbox.verify(alias, hash, signature)) {
            throw new StrongboxException("✘ Invalid signature for existing state!");
        } else {
            Log.i(TAG, "signed state hash verified");
        }
    }
    */


    @Override
    public void close() {
        SQLiteHelper helper = new SQLiteHelper(context);
        db.close();
        helper.close();
        Log.w(TAG, "db closed");
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
}
