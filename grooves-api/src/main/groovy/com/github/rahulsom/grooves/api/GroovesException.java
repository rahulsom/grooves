package com.github.rahulsom.grooves.api;

public class GroovesException extends Exception {
    public GroovesException() {
    }

    public GroovesException(String message) {
        super(message);
    }

    public GroovesException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroovesException(Throwable cause) {
        super(cause);
    }

    public GroovesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
