package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CalendarTodayResponse(
    LocalDate           date,
    List<TodayEventDto> events,
    List<UpcomingDayDto> upcomingDays
) {

}
