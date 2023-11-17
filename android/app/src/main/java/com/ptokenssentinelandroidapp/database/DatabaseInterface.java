package com.ptokenssentinelandroidapp.database;

// NOTE: This interface is use by the rust-lib this java program interacts with.
@SuppressWarnings("unused")
public interface DatabaseInterface {
    byte[] get(byte[] key, byte dataSensitivity) throws Exception;
    void put(byte[] key, byte[] value, byte dataSensitivity);
    void delete(byte[] key);
    String startTransaction();
    String endTransaction();
    void close();
}