package com.fransebastiao.taskmanager.dto.response;

public record StatCardDto(
    String label,
    String value,
    String icon,
    String iconBgClass,
    String iconColorClass,
    String note,
    String noteColorClass
) {}