package com.fransebastiao.taskmanager.exception.custom;

public class BlockedIpException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public BlockedIpException() {
        super();
    }

    public BlockedIpException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BlockedIpException(final String message) {
        super(message);
    }

    public BlockedIpException(final Throwable cause) {
        super(cause);
    }
}

