package com.fransebastiao.taskmanager.exception.custom;

public class S3PresignException extends RuntimeException {
    public S3PresignException(String message, Throwable cause) { super(message, cause); }
}
