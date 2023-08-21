package com.ptokenssentinelandroidapp

import android.content.ContentValues
import android.content.Context
import android.database.AbstractWindowedCursor
import android.database.Cursor
import android.database.CursorWindow
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.util.Pair

class SQLiteHelper(context: Context) : SQLiteOpenHelper(
    context,
    context.getDatabasePath(DATABASE_NAME).absolutePath,
    null,
    DATABASE_VERSION
) {
    init {
        Log.d(
            TAG, "Database path "
                    + context.getDatabasePath(DATABASE_NAME).absolutePath
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DatabaseContract.DatabaseEntry.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DatabaseContract.DatabaseEntry.SQL_UPGRADE_DATABASE)
        onCreate(db)
    }

    companion object {
        private val TAG = SQLiteHelper::class.java.name
        private const val DATABASE_NAME = "StrongboxDatabase"
        private const val DATABASE_VERSION = 2
        private const val CURSOR_WINDOW_NAME = "ValueWindow"
        private const val CURSOR_WINDOW_MAX_BYTES = 80000000
        fun insertOrReplace(db: SQLiteDatabase, key: String?, value: ByteArray?) {
            val contentValues = ContentValues()
            contentValues.put(DatabaseContract.DatabaseEntry.FIELD_KEY, key)
            contentValues.put(DatabaseContract.DatabaseEntry.FIELD_VALUE, value)
            db.replace(DatabaseContract.DatabaseEntry.TABLE_NAME, null, contentValues)
        }

        fun loadExtension(db: SQLiteDatabase) {
            db.execSQL(DatabaseContract.DatabaseEntry.SQL_LOAD_EXTENSION)
            Log.d(TAG, "✔ Extension loaded")
        }

        fun getBytesFromKey(db: SQLiteDatabase, key: String): ByteArray {
            val columns = arrayOf(DatabaseContract.DatabaseEntry.FIELD_VALUE)
            val selection: String = DatabaseContract.DatabaseEntry.FIELD_KEY + " = ?"
            val selectionArgs = arrayOf(key)
            val cursorWindow = CursorWindow(
                CURSOR_WINDOW_NAME,
                CURSOR_WINDOW_MAX_BYTES
                    .toLong()
            )
            val cursor: Cursor = db.query(
                DatabaseContract.DatabaseEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
            )
            val aWindowedCursor = cursor as AbstractWindowedCursor
            aWindowedCursor.window = cursorWindow
            aWindowedCursor.moveToNext()
            return cursor.getBlob(0)
        }

        fun getKeysAndHashedValues(db: SQLiteDatabase): ArrayList<Pair<String, ByteArray>> {
            val cursorWindow = CursorWindow(
                CURSOR_WINDOW_NAME,
                CURSOR_WINDOW_MAX_BYTES
                    .toLong()
            )
            val cursor: Cursor = db.rawQuery(
                DatabaseContract.DatabaseEntry.SQL_GET_ALL_AND_SHA3_VALUES,
                null
            ) 
            val aWindowedCursor: AbstractWindowedCursor = cursor as AbstractWindowedCursor
            aWindowedCursor.window = cursorWindow
            val keyValuePairs = ArrayList<Pair<String, ByteArray>>()
            while (aWindowedCursor.moveToNext()) {
                keyValuePairs.add(
                    Pair(
                        aWindowedCursor.getString(0),
                        aWindowedCursor.getBlob(1)
                    )
                )
            }
            return keyValuePairs
        }

        fun deleteKey(db: SQLiteDatabase, key: String) {
            val selection: String = DatabaseContract.DatabaseEntry.FIELD_KEY + " = ?"
            val selectionArgs = arrayOf(key)
            val n = db.delete(DatabaseContract.DatabaseEntry.TABLE_NAME, selection, selectionArgs)
            Log.d(TAG, "$n records removed")
        }
    }
}
