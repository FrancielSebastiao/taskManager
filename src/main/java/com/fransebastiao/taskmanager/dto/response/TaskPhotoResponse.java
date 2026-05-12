package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskPhotoResponse(
    UUID          id,
    UUID          taskId,
    String        uploadedByName,
    String        url,            // URL presignada válida 60min
    String        extension,
    Long          fileSizeBytes,
    String        caption,
    LocalDateTime uploadedAt
) {}