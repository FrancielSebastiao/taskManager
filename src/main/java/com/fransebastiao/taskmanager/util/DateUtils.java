package com.fransebastiao.taskmanager.util;

import java.time.LocalDate;

import com.fransebastiao.taskmanager.dto.response.DateTimeRange;

public class DateUtils {
    public static DateTimeRange toDateTimeRange(LocalDate from, LocalDate to) {
        return new DateTimeRange(
            from.atStartOfDay(),
            to.plusDays(1).atStartOfDay() // exclusive end
        );
    }
}