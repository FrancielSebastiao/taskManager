package com.fransebastiao.taskmanager.exception.custom;

public class AccountNotVerifiedException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public AccountNotVerifiedException() {
        super();
    }

    public AccountNotVerifiedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AccountNotVerifiedException(final String message) {
        super(message);
    }

    public AccountNotVerifiedException(final Throwable cause) {
        super(cause);
    }
}

