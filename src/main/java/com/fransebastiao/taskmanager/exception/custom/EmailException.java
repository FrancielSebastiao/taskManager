package com.fransebastiao.taskmanager.exception.custom;

public class EmailException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public EmailException() {
        super();
    }

    public EmailException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EmailException(final String message) {
        super(message);
    }

    public EmailException(final Throwable cause) {
        super(cause);
    }
}
