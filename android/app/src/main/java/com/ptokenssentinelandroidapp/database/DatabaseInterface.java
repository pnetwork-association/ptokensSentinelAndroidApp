package com.ptokenssentinelandroidapp.database;

public interface DatabaseInterface {
    byte[] get(byte[] key, byte dataSensitivity) throws Exception;
    void put(byte[] key, byte[] value, byte dataSensitivity);
    void delete(byte[] key);
    void startTransaction() throws DatabaseException;
    void endTransaction() throws DatabaseException;
    void close();
}
