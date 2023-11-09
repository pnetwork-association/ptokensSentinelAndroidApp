package com.ptokenssentinelandroidapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.AbstractWindowedCursor;
import android.database.CursorWindow;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteCursor;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.ptokenssentinelandroidapp.database.Operations;

public class SQLiteHelper extends SQLiteOpenHelper {
    private final static String TAG = SQLiteHelper.class.getName();
    private final static String DATABASE_NAME = "StrongboxDatabase";
    private final static int DATABASE_VERSION = 2;
    private final static String CURSOR_WINDOW_NAME = "ValueWindow";
    private final static int CURSOR_WINDOW_MAX_BYTES = 80000000;

    public SQLiteHelper(Context context) {
        super(
                context,
                context.getDatabasePath(DATABASE_NAME).getAbsolutePath(),
                null,
                DATABASE_VERSION
        );
        Log.d(TAG, "database path "
                + context.getDatabasePath(DATABASE_NAME).getAbsolutePath()
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.DatabaseEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.DatabaseEntry.SQL_UPGRADE_DATABASE);
        onCreate(db);
    }

    static void insertOrReplace(SQLiteDatabase db, String key, byte[] value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.DatabaseEntry.FIELD_KEY, key);
        contentValues.put(DatabaseContract.DatabaseEntry.FIELD_VALUE, value);

        db.replace(DatabaseContract.DatabaseEntry.TABLE_NAME, null, contentValues);
    }

    static byte[] getBytesFromKey(SQLiteDatabase db, String key) {
        String[] columns = { DatabaseContract.DatabaseEntry.FIELD_VALUE };
        String selection = DatabaseContract.DatabaseEntry.FIELD_KEY + " = ?";
        String[] selectionArgs = { key };

        CursorWindow cursorWindow = new CursorWindow(
                CURSOR_WINDOW_NAME,
                CURSOR_WINDOW_MAX_BYTES
        );

        try(SQLiteCursor cursor = (SQLiteCursor) db.query(
                DatabaseContract.DatabaseEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                null
        )) {
            AbstractWindowedCursor aWindowedCursor = (AbstractWindowedCursor) cursor;
            aWindowedCursor.setWindow(cursorWindow);
            aWindowedCursor.moveToNext();
            return cursor.getBlob(0);
        }
    }

    static ArrayList<Pair<String, byte[]>> getKeysAndHashedValues(SQLiteDatabase db) {
        CursorWindow cursorWindow= new CursorWindow(
                CURSOR_WINDOW_NAME,
                CURSOR_WINDOW_MAX_BYTES
        );

        try(SQLiteCursor cursor = (SQLiteCursor) db.rawQuery(
                DatabaseContract.DatabaseEntry.SQL_GET_ALL_AND_SHA3_VALUES,
                null)
        ) {

            AbstractWindowedCursor aWindowedCursor = cursor;
            aWindowedCursor.setWindow(cursorWindow);

            ArrayList<Pair<String, byte[]>> keyValuePairs = new ArrayList<>();
            while(aWindowedCursor.moveToNext()) {
                keyValuePairs.add(new Pair<>(
                        aWindowedCursor.getString(0),
                        aWindowedCursor.getBlob(1))
                );
            }
            return keyValuePairs;
        }
    }

    static void deleteKey(SQLiteDatabase db, String key) {
        String selection = DatabaseContract.DatabaseEntry.FIELD_KEY + " = ?";
        String[] selectionArgs = { key };
        db.delete(DatabaseContract.DatabaseEntry.TABLE_NAME, selection, selectionArgs);
    }

    static void drop(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
