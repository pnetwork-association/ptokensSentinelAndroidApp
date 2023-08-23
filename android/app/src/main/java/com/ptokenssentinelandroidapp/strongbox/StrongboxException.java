package com.ptokenssentinelandroidapp.strongbox;

public class StrongboxException extends Exception {
    @SuppressWarnings("unused")
    public StrongboxException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public StrongboxException(Exception e) {
        super(e);
    }
}
