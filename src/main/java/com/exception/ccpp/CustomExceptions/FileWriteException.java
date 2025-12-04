package com.exception.ccpp.CustomExceptions;

public class FileWriteException extends Exception {
    public FileWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
