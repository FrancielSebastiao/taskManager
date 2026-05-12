package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record TaskActivityDto(
    UUID id,
    String text,
    String user,
    String timeRelative,
    String markerClass
) {}