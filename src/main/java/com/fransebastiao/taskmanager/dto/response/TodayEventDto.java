package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record TodayEventDto(
    UUID        id,
    String      title,
    String      color,
    String      time,       // "09:00 - 12:30"
    int         participants,
    String      location
) {

}
