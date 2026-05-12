package com.fransebastiao.taskmanager.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.request.CreateEventRequest;
import com.fransebastiao.taskmanager.dto.response.CalendarEventDto;
import com.fransebastiao.taskmanager.dto.response.CalendarMonthResponse;
import com.fransebastiao.taskmanager.dto.response.CalendarTodayResponse;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.CalendarService;
import com.fransebastiao.taskmanager.util.RoleUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping
    public ResponseEntity<CalendarEventDto> createEvent(
        @RequestBody CreateEventRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarService.createCalendarEvent(request, userDetails.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEventDto> editEvent(
        @PathVariable UUID id, 
        @RequestBody CreateEventRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(calendarService.editEvent(id, request, userDetails.getId()));
    }

    // GET /api/calendar/month?year=2026&month=4
    @GetMapping("/month")
    public ResponseEntity<CalendarMonthResponse> getMonth(
            @RequestParam int  year,
            @RequestParam int  month,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isPrivileged = RoleUtils.isPrivileged(userDetails.getAuthorities());
        return ResponseEntity.ok(calendarService.getMonth(year, month, userDetails.getId(), isPrivileged));
    }

    // GET /api/calendar/today
    @GetMapping("/today")
    public ResponseEntity<CalendarTodayResponse> getToday(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isPrivileged = RoleUtils.isPrivileged(userDetails.getAuthorities());
        return ResponseEntity.ok(calendarService.getToday(userDetails.getId(), isPrivileged));
    }
}