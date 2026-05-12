package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.calendar.Event;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateEventRequest;
import com.fransebastiao.taskmanager.dto.response.CalendarEventDto;
import com.fransebastiao.taskmanager.dto.response.CalendarMonthResponse;
import com.fransebastiao.taskmanager.dto.response.CalendarTodayResponse;
import com.fransebastiao.taskmanager.dto.response.TodayEventDto;
import com.fransebastiao.taskmanager.dto.response.UpcomingDayDto;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.repository.EventRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.CalendarService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private static final int UPCOMING_DAYS = 4;

    @Transactional
    public CalendarEventDto createCalendarEvent(CreateEventRequest request, UUID userId) {
        User createdBy = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = new Event();
        event.setTitle(request.title());
        event.setLocation(request.location());
        event.setDate(request.date());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setCreatedBy(createdBy);

        if (request.participantIds() != null && !request.participantIds().isEmpty()) {
            List<User> participants = userRepository.findAllById(request.participantIds());
            participants.forEach(event::addParticipant);
        }

        Event saved = eventRepository.save(event);
        return toCalendarEventDto(saved);
    }

    @Transactional
    public CalendarEventDto editEvent(UUID id, CreateEventRequest request, UUID userId) {
        User createdBy = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Event event = eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        event.setTitle(request.title());
        event.setLocation(request.location());
        event.setDate(request.date());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setCreatedBy(createdBy);

        if (request.participantIds() != null && !request.participantIds().isEmpty()) {
            List<User> participants = userRepository.findAllById(request.participantIds());
            participants.forEach(event::addParticipant);
        }

        Event saved = eventRepository.save(event);
        return toCalendarEventDto(saved);
    }

    // ── GET /api/calendar/month ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CalendarMonthResponse getMonth(int year, int month, UUID userId, boolean privileged) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        List<Event> events = eventRepository.findByDateRange(from, to, userId, privileged);

        Map<LocalDate, List<CalendarEventDto>> eventsByDay = events.stream()
            .collect(Collectors.groupingBy(
                Event::getDate,
                TreeMap::new,                           // keeps dates sorted
                Collectors.mapping(this::toCalendarEventDto, Collectors.toList())
            ));

        return new CalendarMonthResponse(year, month, eventsByDay);
    }

    // ── GET /api/calendar/today ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CalendarTodayResponse getToday(UUID userId, boolean privileged) {
        LocalDate today    = LocalDate.now();
        LocalDate upcomingEnd = today.plusDays(UPCOMING_DAYS);

        List<Event> todayEvents = eventRepository.findByDateRange(today, today, userId, privileged);

        List<UpcomingDayDto> upcomingDays = buildUpcomingDays(
            today.plusDays(1), upcomingEnd, userId, privileged
        );

        return new CalendarTodayResponse(
            today,
            todayEvents.stream().map(this::toTodayEventDto).toList(),
            upcomingDays
        );
    }

    // ── Upcoming days builder ─────────────────────────────────────────────────

    private List<UpcomingDayDto> buildUpcomingDays(
            LocalDate from, LocalDate to,
            UUID userId, boolean privileged) {

        // One query for the whole range, then group by date in memory
        Map<LocalDate, Long> countByDate = eventRepository
            .countByDay(from, to, userId, privileged)
            .stream()
            .collect(Collectors.toMap(
                row -> (LocalDate) row[0],
                row -> (Long)      row[1]
            ));

        LocalDate today = LocalDate.now();

        return from.datesUntil(to.plusDays(1))
            .map(date -> new UpcomingDayDto(
                date,
                formatUpcomingLabel(date, today),
                countByDate.getOrDefault(date, 0L)
            ))
            .toList();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private CalendarEventDto toCalendarEventDto(Event e) {
        return new CalendarEventDto(
            e.getId(),
            e.getTitle(),
            e.getColor().name().toLowerCase()
        );
    }

    private TodayEventDto toTodayEventDto(Event e) {
        return new TodayEventDto(
            e.getId(),
            e.getTitle(),
            e.getColor().name().toLowerCase(),
            e.getFormattedTime(),
            e.getParticipantCount(),
            e.getLocation()
        );
    }

    // ── Label helper ──────────────────────────────────────────────────────────

    private String formatUpcomingLabel(LocalDate date, LocalDate today) {
        if (date.equals(today.plusDays(1))) return "Amanhã";

        // "Terça-Feira", "Quarta-Feira" etc.
        return date.getDayOfWeek()
            .getDisplayName(TextStyle.FULL, new Locale("pt", "AO"))
            .substring(0, 1).toUpperCase()
            + date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "AO"))
                .substring(1);
    }
}