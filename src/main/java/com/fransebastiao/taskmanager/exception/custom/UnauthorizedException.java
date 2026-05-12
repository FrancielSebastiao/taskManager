package com.fransebastiao.taskmanager.exception.custom;

public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(final String message) {
        super(message);
    }

    public UnauthorizedException(final Throwable cause) {
        super(cause);
    }
}

