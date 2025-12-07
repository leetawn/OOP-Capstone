package com.exception.ccpp.Debug;

public interface CCLogger {
    void log(String msg);
    void logln(String msg);
    void logf(String format, Object ... args);
    void err(String msg);
    void errln(String msg);
    void errf(String format, Object ... args);
}
