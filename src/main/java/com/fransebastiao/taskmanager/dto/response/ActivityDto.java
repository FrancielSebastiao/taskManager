package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record ActivityDto(
    UUID id,
    String text,
    String userName,
    String timeRelative, // "Há 2 horas", "Ontem"
    String markerClass // "marker--green", "marker--blue"
) {}
