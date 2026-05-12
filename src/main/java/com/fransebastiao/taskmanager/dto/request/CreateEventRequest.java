package com.fransebastiao.taskmanager.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateEventRequest(
    String title,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    List<UUID> participantIds
) {}
