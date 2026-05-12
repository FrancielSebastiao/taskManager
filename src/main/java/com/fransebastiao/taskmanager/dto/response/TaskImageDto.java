package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record TaskImageDto(
    UUID id,
    String url, // URL presignada S3
    String name,
    String uploadedDate,
    String uploadedByInitials,
    String uploadedByColor
) {}
