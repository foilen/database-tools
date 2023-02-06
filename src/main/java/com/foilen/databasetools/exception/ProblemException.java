/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
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
