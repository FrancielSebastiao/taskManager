package com.fransebastiao.taskmanager.service;

import java.util.UUID;

import com.fransebastiao.taskmanager.dto.request.CreateEventRequest;
import com.fransebastiao.taskmanager.dto.response.CalendarEventDto;
import com.fransebastiao.taskmanager.dto.response.CalendarMonthResponse;
import com.fransebastiao.taskmanager.dto.response.CalendarTodayResponse;

public interface CalendarService {
    CalendarEventDto createCalendarEvent(CreateEventRequest request, UUID userId);
    CalendarEventDto editEvent(UUID id, CreateEventRequest request, UUID userId);
    CalendarMonthResponse getMonth(int year, int month, UUID userId, boolean privileged);
    CalendarTodayResponse getToday(UUID userId, boolean privileged);
}
