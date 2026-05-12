package com.fransebastiao.taskmanager.exception.custom;

public class DuplicateException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public DuplicateException() {
        super();
    }

    public DuplicateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DuplicateException(final String message) {
        super(message);
    }

    public DuplicateException(final Throwable cause) {
        super(cause);
    }
}
