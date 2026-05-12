package com.fransebastiao.taskmanager.dto.response;

public record ProjectStatsDto(
    String label,
    String value,
    String icon,
    String iconBgClass,
    String iconColorClass,
    String note,
    String noteColorClass,
    String noteIcon
) {}
