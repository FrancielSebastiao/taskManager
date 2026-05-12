package com.fransebastiao.taskmanager.exception.custom;

public class S3UploadException extends RuntimeException {
    public S3UploadException(String message, Throwable cause) { super(message, cause); }
}
