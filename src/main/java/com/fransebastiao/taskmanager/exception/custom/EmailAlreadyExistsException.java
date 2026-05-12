package com.fransebastiao.taskmanager.exception.custom;

public class EmailAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public EmailAlreadyExistsException() {
        super();
    }

    public EmailAlreadyExistsException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public EmailAlreadyExistsException(final String message) {
        super(message);
    }

    public EmailAlreadyExistsException(final Throwable cause) {
        super(cause);
    }
}

