package com.ptokenssentinelandroidapp

import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import java.util.concurrent.ConcurrentHashMap
import android.util.Log;
import android.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.MessageDigest
//import io.ptokens.security.Strongbox
//import io.ptokens.security.StrongboxException
//import io.ptokens.utils.Operations

class DatabaseWiring(
    private val context: Context,
    private val db: SQLiteDatabase,
    verifyStateHash: Boolean
) : DatabaseInterface {
    private var START_TX_IN_PROGRESS = false
    private var END_TX_IN_PROGRESS = false
    private val cache: MutableMap<String, ByteArray?>
    private val removedKeys: MutableList<String>
    private val verifySignedStateHashEnabled: Boolean
    private var writeSignedStateHashEnabled = false
    private var strongboxEnabled = false

    init {
        removedKeys = ArrayList()
        cache = ConcurrentHashMap()
        verifySignedStateHashEnabled = verifyStateHash
        SQLiteHelper.loadExtension(db)
    }

    constructor(
        context: Context,
        db: SQLiteDatabase,
        verifyStateHash: Boolean,
        writeSignedStateHash: Boolean
    ) : this(context, db, verifyStateHash) {
        writeSignedStateHashEnabled = writeSignedStateHash
    }

    constructor(
        context: Context,
        db: SQLiteDatabase,
        verifyStateHash: Boolean,
        writeSignedStateHash: Boolean,
        strongboxEnabled: Boolean
    ) : this(context, db, verifyStateHash, writeSignedStateHash) {
        this.strongboxEnabled = strongboxEnabled
    }

    override fun put(key: ByteArray?, value: ByteArray?, dataSensitivity: Byte) {
        val hexKey: String = "%02x".format(key);
        removedKeys.remove(hexKey)
        //if (dataSensitivity == 255.toByte()) {
            //cache[hexKey] = Strongbox.encrypt(value)
        //} else {
            cache[hexKey] = value
        //}
        Log.d(TAG, "put: key $hexKey inserted in the cache")
    }

    @Throws(Exception::class)
    override operator fun get(key: ByteArray?, dataSensitivity: Byte): ByteArray? {
        val hexKey: String = "%02x".format(key);
        return try {
            if (removedKeys.contains(hexKey)) {
                Log.d(
                    TAG,
                    "✔ get: value for $hexKey was removed"
                )
                throw Exception("get: key $hexKey not found")
            //} else if (dataSensitivity == 255.toByte()) {
                //try {
                    //Strongbox.decrypt(readCache(hexKey))
                //} catch (e: NullPointerException) {
                    //Strongbox.decrypt(readDatabase(hexKey))
                //}
            } else {
                try {
                    readCache(hexKey)
                } catch (e: NullPointerException) {
                    Log.d(
                        TAG,
                        "✔ get: value for $hexKey not in cache, checking db..."
                    )
                    readDatabase(hexKey)
                }
            }
        } catch (e: CursorIndexOutOfBoundsException) {
            throw Exception("✘ get: key $hexKey not stored inside the db")
        }
    }

    override fun delete(key: ByteArray?) {
        val hexKey: String = "%02x".format(key);
        removedKeys.add(hexKey)
    }

    @Throws(DatabaseException::class)
    override fun startTransaction() {
        Log.i(TAG, "✔ Start transaction in progress!")
        if (START_TX_IN_PROGRESS) {
            return
        }
        START_TX_IN_PROGRESS = true
        //if (verifySignedStateHashEnabled) {
            //try {
                //verifySignedStateHash()
            //} catch (e: StrongboxException) {
                //Log.e(TAG, "Signed state hash verification failed!", e)
                //throw DatabaseException("Start transaction failed")
            //}
        //} else {
            Log.i(TAG, "✔ Signed state hash verification skipped")
        //}
        db.beginTransaction()
    }

    @Throws(DatabaseException::class)
    override fun endTransaction() {
        Log.i(TAG, "✔ endTransaction in progress... ")
        try {
            if (END_TX_IN_PROGRESS) {
                return
            }
            START_TX_IN_PROGRESS = if (!START_TX_IN_PROGRESS) {
                throw DatabaseException(
                    "✘ Invalid order, call startTransaction first!"
                )
            } else {
                false
            }
            END_TX_IN_PROGRESS = true
            Log.v(TAG, "✔ Writing keys: ")
            for (key in cache.keys) {
                SQLiteHelper.insertOrReplace(db, key, cache[key])
            }
            for (key in removedKeys) {
                SQLiteHelper.deleteKey(db, key)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        //try {
            //if (writeSignedStateHashEnabled) {
                //writeSignedStateHash()
            //} else {
                Log.w(TAG, "✔ Skipping state hash writing...")
            //}
        //} catch (e: DatabaseException) {
            //Log.e(TAG, "✘ Failed to write the state hash", e)
        //} finally {
            START_TX_IN_PROGRESS = false
        //}
    }

    /*
    @Throws(DatabaseException::class)
    private fun writeSignedStateHash() {
        val hash = currentStateHash
            ?: throw DatabaseException("Write signed state failed, hash not found")
        val aliasNumber: Int = Strongbox.getLatestAliasNumber()
        val oldAlias: String = (Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber)
        val newAlias: String = (Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + (aliasNumber + 1))
        Strongbox.generateSigningKey(newAlias, strongboxEnabled)
        Log.i(TAG, "✔ Switch key $oldAlias <=> $newAlias")
        val signedState: ByteArray = Strongbox.sign(newAlias, hash)
        Log.i(
            TAG, "✔ New signed state hash " +
                    Base64.encodeToString(signedState, Base64.DEFAULT)
        )
        Operations.writeBytes(context, NAME_SIGNED_STATE_HASH, signedState)
        Strongbox.removeKey(oldAlias)
    }
    */

    private val currentStateHash: ByteArray?
        private get() {
            try {
                val keyValuePairs: ArrayList<android.util.Pair<String, ByteArray>> =
                    SQLiteHelper.getKeysAndHashedValues(db)
                if (keyValuePairs.isEmpty()) {
                    Log.w(TAG, "✔ No keys found!")
                    return null
                }
                val sha1 = MessageDigest.getInstance("SHA-1")
                for (keyValue in keyValuePairs) {
                    val key = keyValue.first
                    sha1.update(key.toByteArray())
                    sha1.update(keyValue.second)
                }
                val currentStateHash = sha1.digest()
                Log.i(
                    TAG, "✔ Current state hash"
                            + Base64.encodeToString(currentStateHash, Base64.DEFAULT)
                )
                return currentStateHash
            } catch (e: NoSuchAlgorithmException) {
                Log.e(TAG, "✘ SHA-1 not supported", e)
            } catch (e: Exception) {
                Log.e(TAG, "✘ Current state hash error:" + e.message)
            }
            return null
        }

    /*
    @Throws(StrongboxException::class)
    private fun verifySignedStateHash() {
        val signature: ByteArray = Operations.readBytes(context, NAME_SIGNED_STATE_HASH)
        val hash = currentStateHash
        val signatureExists = signature != null && signature.size > 0
        val hashExists = hash != null
        if (!signatureExists && hashExists) {
            throw StrongboxException("✘ Missing signature for existing state!")
        } else if (signatureExists && !hashExists) {
            throw StrongboxException("✘ Existing signature for missing state!")
        } else if (!signatureExists) {
            Log.i(TAG, "✔ First run!")
            return
        }

        // Reached this point the we have a signature and
        // a state hash
        val aliasNumber: Int = Strongbox.getLatestAliasNumber()
        if (aliasNumber == -1) {
            throw StrongboxException("✘ Unverifiable signature for existing state! Aborting!")
        }
        val alias: String = (Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber)
        if (!Strongbox.verify(alias, hash, signature)) {
            throw StrongboxException("✘ Invalid signature for existing state!")
        } else {
            Log.i(TAG, "✔ Signed state hash verified")
        }
    }
    */

    override fun close() {
        val helper = SQLiteHelper(context)
        db.close()
        helper.close()
        Log.w(TAG, "Db closed")
    }

    private fun readCache(hexKey: String): ByteArray {
        return cache[hexKey]
            ?: throw NullPointerException("Value not found in the cache")
    }

    private fun readDatabase(hexKey: String): ByteArray {
        return SQLiteHelper.getBytesFromKey(db, hexKey)
            ?: throw NullPointerException("Value not found in the database")
    }

    companion object {
        val TAG = DatabaseWiring::class.java.name
        private const val NAME_SIGNED_STATE_HASH = "state-hash.sig"
    }
}
