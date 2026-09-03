package com.foilen.databasetools.manage.exception;

public class RetryLaterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private long retryInMs;

    public RetryLaterException(String message, long retryInMs) {
        super(message);
        this.retryInMs = retryInMs;
    }

    public RetryLaterException(String message, long retryInMs, Throwable cause) {
        super(message, cause);
        this.retryInMs = retryInMs;
    }

    public long getRetryInMs() {
        return retryInMs;
    }

}
