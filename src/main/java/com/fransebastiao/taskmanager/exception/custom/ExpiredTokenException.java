package com.fransebastiao.taskmanager.exception.custom;

public class ExpiredTokenException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public ExpiredTokenException() {
        super();
    }

    public ExpiredTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExpiredTokenException(final String message) {
        super(message);
    }

    public ExpiredTokenException(final Throwable cause) {
        super(cause);
    }
}

