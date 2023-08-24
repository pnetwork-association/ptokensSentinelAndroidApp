package com.ptokenssentinelandroidapp.database;

final class DatabaseContract {
    /**
     * Do not allow class instantiation
     */
    private DatabaseContract() { }

    static class DatabaseEntry {
        final static String FIELD_ID = "id";
        final static String FIELD_KEY = "hexKey";
        final static String FIELD_VALUE = "hexValue";
        final static String TABLE_NAME = "StrongboxStorage";
        final static String SQL_UPGRADE_DATABASE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        final static String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FIELD_KEY + " TEXT NOT NULL UNIQUE,"
                + FIELD_VALUE + " BLOB NOT NULL "
                + ");";
        final static String SQL_GET_ALL_AND_SHA3_VALUES = "SELECT "
                + FIELD_KEY
                + ", sha3(" + FIELD_VALUE +") FROM "
                + TABLE_NAME;
        //final static String SQL_LOAD_EXTENSION = "SELECT load_extension('../libshathree.so');";
    }
}
