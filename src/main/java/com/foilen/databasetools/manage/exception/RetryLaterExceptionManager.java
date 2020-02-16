/*
    Database Tools
    https://github.com/foilen/database-tools
    Copyright (c) 2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.databasetools.manage.exception;

public class RetryLaterExceptionManager extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private long retryInMs;

    public RetryLaterExceptionManager(String message, long retryInMs) {
        super(message);
        this.retryInMs = retryInMs;
    }

    public RetryLaterExceptionManager(String message, long retryInMs, Throwable cause) {
        super(message, cause);
        this.retryInMs = retryInMs;
    }

    public long getRetryInMs() {
        return retryInMs;
    }

}
