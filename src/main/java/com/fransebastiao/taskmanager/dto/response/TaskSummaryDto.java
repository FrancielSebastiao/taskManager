package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record TaskSummaryDto(
    UUID id,
    String name,
    boolean completed,
    String assigneeInitials,
    String assigneeColor,
    String dueDateRelative, // "Hoje", "Amanhã", "Há 2 dias"
    String priority
) {}
