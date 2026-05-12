package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.dto.response.AssigneeAvatarDto;
import com.fransebastiao.taskmanager.dto.response.DashboardMetricsDto;
import com.fransebastiao.taskmanager.dto.response.DashboardRecentActivityDto;
import com.fransebastiao.taskmanager.dto.response.DashboardResponse;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.StatCardDto;
import com.fransebastiao.taskmanager.dto.response.TimelinePointDto;
import com.fransebastiao.taskmanager.dto.response.UpcomingTaskDto;
import com.fransebastiao.taskmanager.repository.AnalyticsRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.service.DashboardService;
import com.fransebastiao.taskmanager.util.AvatarHelper;

import lombok.RequiredArgsConstructor;
  
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private final TaskRepository taskRepository;
    private final AnalyticsRepository analyticsRepository;
    private final AvatarHelper avatarHelper;
    private static final int DEFAULT_PAGE_SIZE = 10;

    // ==============================
    // 🚀 PUBLIC ENTRY POINT
    // ==============================
    public DashboardResponse getDashboard(
        LocalDate from,
        LocalDate to,
        UUID userId,
        boolean isPrivileged
    ) {

        DashboardMetricsDto current =
            analyticsRepository.buildMetrics(from, to, userId, isPrivileged);

        long days = ChronoUnit.DAYS.between(from, to) + 1;

        LocalDate prevFrom = from.minusDays(days);
        LocalDate prevTo   = from.minusDays(1);

        DashboardMetricsDto previous =
            analyticsRepository.buildMetrics(prevFrom, prevTo, userId, isPrivileged);

        return new DashboardResponse(
            List.of(
                buildTotalTasksStat(current, previous),           // Card 1: Total (all-time)
                buildInProgressStat(current, previous),           // Card 2: In Progress (snapshot)
                buildTotalCompletedStat(current, previous),       // Card 3: Total Completed (all-time)
                buildTeamStat(current, previous)                  // Card 4: Team
            ),
            analyticsRepository.getTimeline(from, to, userId, isPrivileged),
            getRecentActivities(userId, isPrivileged, 0),
            getUpcomingTasks(userId, isPrivileged, 0)
        );
    }

    public List<TimelinePointDto> getTimeline(LocalDate from, LocalDate to, UUID userId, boolean privileged) {
        return analyticsRepository.getTimeline(from, to, userId, privileged);
    }

    public PagedResponse<UpcomingTaskDto> getUpcomingTasks(UUID userId, boolean privileged, int pageIndex) {
        Pageable pageable = PageRequest.of(pageIndex, DEFAULT_PAGE_SIZE,
            Sort.by("createdAt").descending());

        if (privileged) {
            return PagedResponse.of(taskRepository.findUpcomingTasks(pageable)
                .map(this::toUpcomingTaskDto));  // ← map to DTO
        }
        return PagedResponse.of(taskRepository.findUpcomingTasksByAssignee(userId, pageable)
            .map(this::toUpcomingTaskDto));      // ← map to DTO
    }

    public PagedResponse<DashboardRecentActivityDto> getRecentActivities(UUID userId, boolean privileged, int pageIndex) {
        Pageable pageable = PageRequest.of(pageIndex, DEFAULT_PAGE_SIZE,
            Sort.by("createdAt").descending());

        if (privileged) {
            return PagedResponse.of(taskRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toRecentActivityDto));
        }

        return PagedResponse.of(taskRepository.findByAssignee(userId, pageable)
            .map(this::toRecentActivityDto));
    }

    // ==============================
    // 📦 STAT BUILDERS (UI LAYER)
    // ==============================

    /**
     * Shows TOTAL activities (all time) with comparison based on activities CREATED this period
     * Example: "230 total (↗ 50% desde o mês passado)" 
     *          means 30 created this month vs 20 last month
     */
    private StatCardDto buildTotalTasksStat(DashboardMetricsDto cur, DashboardMetricsDto prev) {
        double delta = calcDeltaPercent(cur.createdInPeriod(), prev.createdInPeriod());

        return new StatCardDto(
            "Total de Atividades",
            String.valueOf(cur.total()),
            "task_alt",
            "icon-bg--green",
            "icon--green",
            formatTrendNote(delta, "desde o período anterior"),
            determineTrend(delta)
        );
    }

    /**
     * Card 2: Tasks currently in progress (snapshot)
     * Shows current number with optional trend if significant change
     */
    private StatCardDto buildInProgressStat(DashboardMetricsDto cur, DashboardMetricsDto prev) {
        long diff = cur.inProgress() - prev.inProgress();
        
        // Only show trend if change is significant (±3 tasks)
        String trendNote = Math.abs(diff) >= 3
            ? formatTrendNote(calcDeltaPercent(cur.inProgress(), prev.inProgress()), "vs período anterior")
            : String.format("%d em progresso", cur.inProgress());

        return new StatCardDto(
            "Em Progresso",
            String.valueOf(cur.inProgress()),
            "schedule",
            "icon-bg--blue",
            "icon--blue",
            trendNote,
            determineTrend(diff)
        );
    }

    /**
     * Card 3: Total completed tasks (all time) with comparison based on completed THIS period
     * Example: "150 completas (↗ 25%)" means 25 completed this period vs 20 last period
     */
    private StatCardDto buildTotalCompletedStat(DashboardMetricsDto cur, DashboardMetricsDto prev) {
        double delta = calcDeltaPercent(cur.completedInPeriod(), prev.completedInPeriod());

        return new StatCardDto(
            "Atividades Completas",
            String.valueOf(cur.completed()),  // ← Total completed (all-time)
            "check_circle",
            "icon-bg--green",
            "icon--green",
            formatTrendNote(delta, "desde o período anterior"),
            determineTrend(delta)
        );
    }

    /**
     * Card 4: Team size (snapshot)
     */
    private StatCardDto buildTeamStat(DashboardMetricsDto cur, DashboardMetricsDto prev) {
        long diff = cur.teamSize() - prev.teamSize();
        
        String trendNote = Math.abs(diff) >= 2 
            ? formatTrendNote(calcDeltaPercent(cur.teamSize(), prev.teamSize()), "vs período anterior")
            : String.format("%d membros ativos", cur.teamSize());

        return new StatCardDto(
            "Equipa",
            String.valueOf(cur.teamSize()),
            "group",
            "icon-bg--purple",
            "icon--purple",
            trendNote,
            determineTrend(diff)
        );
    }

    // ==============================
    // MAPPERS
    // ==============================
    private DashboardRecentActivityDto toRecentActivityDto(Task t) {
        List<AssigneeAvatarDto> assignees = t.getAssignees() == null
            ? List.of()
            : t.getAssignees().stream()
                .map(avatarHelper::toAssigneeAvatar)
                .toList();

        return new DashboardRecentActivityDto(
            t.getId(),
            t.getTitle(),
            t.getCategory() != null ? t.getCategory().getName() : null,
            t.getStatus(),
            t.getCreatedAt(),
            t.getCompletedAt(),
            assignees
        );
    }

    private UpcomingTaskDto toUpcomingTaskDto(Task t) {
        List<AssigneeAvatarDto> assignees = t.getAssignees() == null
            ? List.of()
            : t.getAssignees().stream()
                .map(avatarHelper::toAssigneeAvatar)
                .toList();

        return new UpcomingTaskDto(
            t.getId(),
            t.getTitle(),
            t.getDueDate(),
            t.getStatus(),
            t.getPriority(),
            t.getProgressPercent(),
            assignees
        );
    }


        // ==============================
    // 📈 HELPERS
    // ==============================

    private String formatTrendNote(double delta, String suffix) {
        if (delta == 0) {
            return "→ Sem alteração " + suffix;
        }
        String arrow = delta > 0 ? "↗" : "↘";
        return String.format("%s %.1f%% %s", arrow, Math.abs(delta), suffix);
    }

    private String determineTrend(double delta) {
        if (delta > 0) return "trend--up";
        if (delta < 0) return "trend--down";
        return "trend--neutral";
    }

    private double calcDeltaPercent(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        double raw = (current - previous) * 100.0 / previous;
        return Math.round(raw * 10.0) / 10.0;
    }
}