package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record TaskFileDto(
    UUID id,
    String name,
    String size,
    String icon,
    String iconBgClass,
    String iconColorClass,
    String url // URL presignada S3
) {}
