package com.ptokenssentinelandroidapp.strongbox;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public interface StrongboxInterface {
    //byte[] get(byte[] key, byte dataSensitivity) throws Exception;
    boolean keystoreIsInitialized() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException;
    //void put(byte[] key, byte[] value, byte dataSensitivity);
    //void delete(byte[] key);
    //void startTransaction() throws DatabaseException;
    //void endTransaction() throws DatabaseException;
    //void close();
}
