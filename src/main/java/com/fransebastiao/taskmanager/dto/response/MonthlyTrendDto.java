package com.fransebastiao.taskmanager.dto.response;

public record MonthlyTrendDto(
    String month,
    Long   created,
    Long   completed
) {}