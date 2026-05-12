package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record CalendarEventDto(
    UUID        id,
    String      title,
    String      color  
) {}
