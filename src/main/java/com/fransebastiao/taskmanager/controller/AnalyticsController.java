package com.fransebastiao.taskmanager.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.response.AnalyticsDashboardResponse;
import com.fransebastiao.taskmanager.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('LER_RELATORIOS')")
    public ResponseEntity<AnalyticsDashboardResponse> getDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Default: mês actual
        LocalDate dateTo   = to   != null ? to   : LocalDate.now();
        LocalDate dateFrom = from != null ? from : dateTo.withDayOfMonth(1);

        return ResponseEntity.ok(analyticsService.getDashboard(dateFrom, dateTo));
    }
}