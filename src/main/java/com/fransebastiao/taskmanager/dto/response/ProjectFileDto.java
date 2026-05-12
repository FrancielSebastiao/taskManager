package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record ProjectFileDto(
UUID id,
String name,
String size,
String icon,
String iconBgClass,
String iconColorClass,
String url // URL presignada do S3
) {}
