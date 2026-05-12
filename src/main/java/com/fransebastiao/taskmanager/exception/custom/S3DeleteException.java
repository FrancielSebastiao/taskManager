package com.fransebastiao.taskmanager.exception.custom;

public class S3DeleteException extends RuntimeException {
    public S3DeleteException(String message, Throwable cause) { super(message, cause); }
}