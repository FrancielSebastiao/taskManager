package com.fransebastiao.taskmanager.service;

import java.io.InputStream;

public interface S3Service {
    void delete(String s3Key);
    String gerarUrlPresignada(String s3Key);
    String upload(String s3Key, InputStream inputStream, long contentLength, String contentType);
}
