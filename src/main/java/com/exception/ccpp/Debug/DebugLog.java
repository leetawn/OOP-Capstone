package com.exception.ccpp.Debug;

// temporarily just prints to sout
// soon it will write to a single log file
public class DebugLog implements CCLogger {
    public static final boolean DEBUG_ENABLED = false;
    private static DebugLog instance;
    private DebugLog() {}

    public static DebugLog getInstance() {
        if (instance == null) {
            instance = new DebugLog();
        }
        return instance;
    }

    @Override
    public void log(String msg) {
        if (DEBUG_ENABLED) {
            System.out.print(msg);
        }
    }

    // INSTANCE
    public void logln(String msg) {
        if (DEBUG_ENABLED) {
            System.out.println(msg);
        }
    }
    public void logf(String format, Object ... args) {
        if (DEBUG_ENABLED) {
            System.out.printf(format, args);
        }
    }

    @Override
    public void err(String msg) {
        if (DEBUG_ENABLED) {
            System.err.print(msg);
        }
    }

    public void errln(String msg) {
        if (DEBUG_ENABLED) {
            System.err.println(msg);
        }
    }
    public void errf(String format, Object ... args) {
        if (DEBUG_ENABLED) {
            System.err.printf(format, args);
        }
    }
}