package com.ptokenssentinelandroidapp

internal class DatabaseContract

private constructor() {
    internal object DatabaseEntry {
        const val FIELD_ID = "id"
        const val FIELD_KEY = "hexKey"
        const val FIELD_VALUE = "hexValue"
        const val TABLE_NAME = "StrongboxStorage"
        const val SQL_UPGRADE_DATABASE = "DROP TABLE IF EXISTS " + TABLE_NAME
        const val SQL_CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FIELD_KEY + " TEXT NOT NULL UNIQUE,"
                + FIELD_VALUE + " BLOB NOT NULL "
                + ");")
        const val SQL_GET_ALL_AND_SHA3_VALUES = ("SELECT "
                + FIELD_KEY
                + ", sha3(" + FIELD_VALUE + ") FROM "
                + TABLE_NAME)
        const val SQL_LOAD_EXTENSION = "SELECT load_extension('libshathree.so');"
    }
}