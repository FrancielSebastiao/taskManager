package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record CalendarMonthResponse(
    int                                     year,
    int                                     month,
    Map<LocalDate, List<CalendarEventDto>>  eventsByDay
) {

}
