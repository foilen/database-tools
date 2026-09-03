package com.foilen.databasetools.exception;

public class ProblemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProblemException(String message) {
        super(message);
    }

    public ProblemException(String message, Throwable cause) {
        super(message, cause);
    }

}
