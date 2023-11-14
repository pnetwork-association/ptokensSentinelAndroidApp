package com.ptokenssentinelandroidapp.rustlogger;

public class RustLogger {
    public static native void log(String logLevel, String logMessage);

    public static void rustLog(String logLevel, String logMessage) {
        log(logLevel, logMessage);
    }

    public static void rustLog(String logMessage) {
        // NOTE: If no log level is passed, we assume it's a debug log
        rustLog("debug", logMessage);
    }
}