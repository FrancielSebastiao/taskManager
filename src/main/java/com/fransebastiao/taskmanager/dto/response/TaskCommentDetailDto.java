package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record TaskCommentDetailDto(
    UUID id,
    String userName,
    String userInitials,
    String userColor,
    String text,
    String categoryLabel,
    String timeRelative,
    String attachmentName, // null se sem anexo
    String attachmentUrl // null se sem anexo
) {}
