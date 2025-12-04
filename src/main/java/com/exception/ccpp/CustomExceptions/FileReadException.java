package com.exception.ccpp.CustomExceptions;

public class FileReadException extends Exception {
    public FileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
