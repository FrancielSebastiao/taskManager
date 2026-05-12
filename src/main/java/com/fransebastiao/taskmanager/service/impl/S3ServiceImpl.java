package com.fransebastiao.taskmanager.service.impl;

import java.io.InputStream;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fransebastiao.taskmanager.exception.custom.S3DeleteException;
import com.fransebastiao.taskmanager.exception.custom.S3PresignException;
import com.fransebastiao.taskmanager.exception.custom.S3UploadException;
import com.fransebastiao.taskmanager.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client    s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.aws.bucket}")
    private String bucket;

    public String upload(String s3Key, InputStream inputStream, long contentLength, String contentType) {
        try {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, contentLength)
            );

            log.info("File uploaded to S3: {}", s3Key);
            return s3Key;

        } catch (S3Exception e) {
            log.error("S3 upload failed for key {}: {}", s3Key, e.getMessage());
            throw new S3UploadException("Failed to upload file to S3", e);
        }
    }

    public void delete(String s3Key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build());
            log.info("File deleted from S3: {}", s3Key);
        } catch (S3Exception e) {
            log.error("S3 delete failed for key {}: {}", s3Key, e.getMessage());
            throw new S3DeleteException("Failed to delete file from S3", e);
        }
    }

    // Gera URL temporária válida por 60 minutos
    public String gerarUrlPresignada(String s3Key) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(60))
                    .getObjectRequest(r -> r.bucket(bucket).key(s3Key))
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL for key {}: {}", s3Key, e.getMessage());
            throw new S3PresignException("Failed to generate presigned URL", e);
        }
    }
}