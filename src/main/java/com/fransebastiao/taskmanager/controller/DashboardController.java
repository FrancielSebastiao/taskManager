package com.fransebastiao.taskmanager.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.response.DashboardResponse;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.DashboardRecentActivityDto;
import com.fransebastiao.taskmanager.dto.response.TimelinePointDto;
import com.fransebastiao.taskmanager.dto.response.UpcomingTaskDto;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.DashboardService;
import com.fransebastiao.taskmanager.util.DashboardPeriod;
import com.fransebastiao.taskmanager.util.RoleUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(defaultValue = "LAST_30_DAYS") DashboardPeriod period
    ) {
        LocalDate[] range = period.toDateRange();

        boolean isPrivileged = RoleUtils.isPrivileged(userDetails.getAuthorities());

        return ResponseEntity.ok(
            dashboardService.getDashboard(
                range[0],
                range[1],
                userDetails.getId(),
                isPrivileged
            )
        );
    }

    // ==============================
    // GET /api/dashboard/timeline
    // ==============================
    @GetMapping("/timeline")
    public ResponseEntity<List<TimelinePointDto>> getTimeline(
            @RequestParam DashboardPeriod period,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        LocalDate[] range = period.toDateRange();   // ← use period, drop from/to
        boolean isPrivileged = RoleUtils.isPrivileged(userDetails.getAuthorities());
        return ResponseEntity.ok(dashboardService.getTimeline(range[0], range[1], userDetails.getId(), isPrivileged));
    }

    // ==============================
    // GET /api/dashboard/upcoming-tasks
    // ==============================
    @GetMapping("/upcoming-tasks")
    public ResponseEntity<PagedResponse<UpcomingTaskDto>> getUpcomingTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir
    ) {
        boolean isPrivileged = RoleUtils.isPrivileged(userDetails.getAuthorities());
        return ResponseEntity.ok(dashboardService.getUpcomingTasks(userDetails.getId(), isPrivileged, page));
    }

    // ==============================
    // GET /api/dashboard/recent-activities
    // ==============================
    @GetMapping("/recent-activities")
    public ResponseEntity<PagedResponse<DashboardRecentActivityDto>> getRecentActivities(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page
    ) {
        boolean isPrivileged = RoleUtils.isPrivileged(userDetails.getAuthorities());
        return ResponseEntity.ok(dashboardService.getRecentActivities(userDetails.getId(), isPrivileged, page));
    }
}
