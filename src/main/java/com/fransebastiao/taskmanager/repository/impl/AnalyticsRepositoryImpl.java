package com.fransebastiao.taskmanager.repository.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.task.Task.TaskStatus;
import com.fransebastiao.taskmanager.dto.response.CategoryDistributionDto;
import com.fransebastiao.taskmanager.dto.response.DashboardMetricsDto;
import com.fransebastiao.taskmanager.dto.response.DateTimeRange;
import com.fransebastiao.taskmanager.dto.response.LaborSummaryDto;
import com.fransebastiao.taskmanager.dto.response.MonthlyTrendDto;
import com.fransebastiao.taskmanager.dto.response.RecentActivityDto;
import com.fransebastiao.taskmanager.dto.response.StatusDistributionDto;
import com.fransebastiao.taskmanager.dto.response.TaskMetricsDto;
import com.fransebastiao.taskmanager.dto.response.TeamPerformanceDto;
import com.fransebastiao.taskmanager.dto.response.TimelinePointDto;
import com.fransebastiao.taskmanager.repository.AnalyticsRepository;
import com.fransebastiao.taskmanager.util.DateUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final EntityManager em;

    @Override
    public TaskMetricsDto getTaskMetrics(LocalDate from, LocalDate to) {
        DateTimeRange range = DateUtils.toDateTimeRange(from, to);
        // Total de tarefas no período
        Long total = em.createQuery("""
                SELECT COUNT(t) FROM Task t
                WHERE t.createdAt >= :from AND t.createdAt < :to
                """, Long.class)
                .setParameter("from", range.from())
                .setParameter("to", range.to())
                .getSingleResult();

        // Concluídas no período
        Long completed = em.createQuery("""
            SELECT COUNT(t) FROM Task t
            WHERE t.completedAt >= :from
            AND t.completedAt < :to
            AND t.status = 'COMPLETA'
            """, Long.class)
            .setParameter("from", range.from())
            .setParameter("to", range.to())
            .getSingleResult();

        // Utilizadores activos (com tarefas atribuídas no período)
        Long activeUsers = em.createQuery("""
                SELECT COUNT(DISTINCT u) FROM User u
                JOIN u.assignedTasks t
                WHERE t.createdAt >= :from AND t.createdAt < :to
                """, Long.class)
                .setParameter("from", range.from())
                .setParameter("to", range.to())
                .getSingleResult();

        // Tempo médio de conclusão (em dias) via LaborEntry
        Double avgDays = em.createQuery("""
                SELECT AVG(DATEDIFF(l.actualEndDate, l.startDate))
                FROM LaborEntry l
                WHERE l.actualEndDate IS NOT NULL
                AND l.actualEndDate >= :from AND l.actualEndDate <= :to
                """, Double.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return new TaskMetricsDto(total, completed, activeUsers,
                avgDays != null ? Math.round(avgDays * 10.0) / 10.0 : 0.0);
    }

    @Override
    public List<MonthlyTrendDto> getMonthlyTrend(int months) {

        List<MonthlyTrendDto> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {

            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            String label = monthStart.getMonth()
                .getDisplayName(TextStyle.SHORT, new Locale("pt", "PT"));

            LocalDateTime from = monthStart.atStartOfDay();
            LocalDateTime to = monthEnd.plusDays(1).atStartOfDay();

            Long created = em.createQuery("""
                SELECT COUNT(t) FROM Task t
                WHERE t.createdAt >= :from
                AND t.createdAt < :to
                """, Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

            Long completed = em.createQuery("""
                SELECT COUNT(t) FROM Task t
                WHERE t.completedAt >= :from
                AND t.completedAt < :to
                AND t.status = 'COMPLETA'
                """, Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

            result.add(new MonthlyTrendDto(label, created, completed));
        }

        return result;
    }

    @Override
    public List<StatusDistributionDto> getStatusDistribution(LocalDate from, LocalDate to) {
        return em.createQuery("""
                SELECT new com.fransebastiao.taskmanager.dto.response.StatusDistributionDto(
                    t.status, COUNT(t))
                FROM Task t
                WHERE t.createdAt >= :from AND t.createdAt < :to
                GROUP BY t.status
                """, StatusDistributionDto.class)
                .setParameter("from", from.atStartOfDay())
                .setParameter("to", to.plusDays(1).atStartOfDay())
                .getResultList();
    }

    @Override
    public List<TeamPerformanceDto> getTeamPerformance(LocalDate from, LocalDate to) {
        return em.createQuery("""
                SELECT new com.fransebastiao.taskmanager.dto.response.TeamPerformanceDto(
                    u.name,
                    COUNT(t),
                    SUM(CASE WHEN t.status = 'COMPLETA' THEN 1 ELSE 0 END))
                FROM User u
                JOIN u.assignedTasks t
                WHERE t.createdAt >= :from AND t.createdAt < :to
                GROUP BY u.id, u.name
                ORDER BY SUM(CASE WHEN t.status = 'COMPLETA' THEN 1 ELSE 0 END) DESC
                """, TeamPerformanceDto.class)
                .setParameter("from", from.atStartOfDay())
                .setParameter("to", to.plusDays(1).atStartOfDay())
                .getResultList();
    }

    @Override
    public List<CategoryDistributionDto> getCategoryDistribution(LocalDate from, LocalDate to) {
        return em.createQuery("""
                SELECT new com.fransebastiao.taskmanager.dto.response.CategoryDistributionDto(
                    c.name, COUNT(t))
                FROM Task t
                JOIN t.category c
                WHERE t.createdAt >= :from AND t.createdAt < :to
                GROUP BY c.id, c.name
                ORDER BY COUNT(t) DESC
                """, CategoryDistributionDto.class)
                .setParameter("from", from.atStartOfDay())
                .setParameter("to", to.plusDays(1).atStartOfDay())
                .getResultList();
    }

    @Override
    public List<LaborSummaryDto> getLaborSummary(LocalDate from, LocalDate to) {
        return em.createQuery("""
                SELECT new com.fransebastiao.taskmanager.dto.response.LaborSummaryDto(
                    u.name,
                    t.title,
                    l.startDate,
                    l.expectedEndDate,
                    l.actualEndDate,
                    l.agreedAmount)
                FROM LaborEntry l
                JOIN l.worker u
                JOIN l.task t
                WHERE l.startDate >= :from AND l.startDate <= :to
                ORDER BY l.startDate DESC
                """, LaborSummaryDto.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    @Override
    public List<RecentActivityDto> getRecentActivities(int limit) {
        return em.createQuery("""
                SELECT new com.fransebastiao.taskmanager.dto.response.RecentActivityDto(
                    t.id,
                    t.title,
                    c.name,
                    t.status,
                    t.createdAt,
                    t.completedAt)
                FROM Task t
                LEFT JOIN t.category c
                ORDER BY t.createdAt DESC
                """, RecentActivityDto.class)
                .setMaxResults(limit)
                .getResultList();
    }

    // ==============================
    // ✅ UPDATED REPOSITORY
    // ==============================

    public DashboardMetricsDto buildMetrics(
            LocalDate from,
            LocalDate to,
            UUID userId,
            boolean privileged
        ) {

        DateTimeRange range = DateUtils.toDateTimeRange(from, to);

        // ALL-TIME TOTALS
        long total = count("", userId, privileged);

        long totalCompleted = count("""
            AND t.status = 'COMPLETA'
        """, userId, privileged);

        // PERIOD-BASED COUNTS
        long createdInPeriod = count("""
            AND t.createdAt >= :from AND t.createdAt < :to
        """, userId, privileged, range);

        long completedInPeriod = count("""
            AND t.status = 'COMPLETA'
            AND t.completedAt >= :from AND t.completedAt < :to
        """, userId, privileged, range);

        // SNAPSHOT COUNTS
        long inProgress = count("""
            AND t.status = 'EM_PROGRESSO'
        """, userId, privileged);

        long overdue = count("""
            AND t.status != 'COMPLETA'
            AND t.dueDate < CURRENT_DATE
        """, userId, privileged);

        long teamSize = privileged
            ? em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult()
            : em.createQuery("""
                SELECT COUNT(DISTINCT u2.id)
                FROM Task t
                JOIN t.assignees u1
                JOIN t.assignees u2
                WHERE u1.id = :userId
            """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        return new DashboardMetricsDto(
            total,
            createdInPeriod,
            totalCompleted,
            completedInPeriod,
            inProgress,
            overdue,
            teamSize
        );
    }

    // =============================
    // 🔧 GENERIC COUNT METHOD (unchanged)
    // =============================
    private long count(
        String extraConditions,
        UUID userId,
        boolean privileged
    ) {
        return count(extraConditions, userId, privileged, null);
    }

    private long count(
        String extraConditions,
        UUID userId,
        boolean privileged,
        DateTimeRange range
    ) {

        String base = """
            SELECT COUNT(t)
            FROM Task t
            %s
        """;

        String join = privileged
            ? "WHERE 1=1"
            : "JOIN t.assignees u WHERE u.id = :userId";

        String jpql = base.formatted(join) + extraConditions;

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);

        if (!privileged) {
            query.setParameter("userId", userId);
        }

        if (range != null) {
            query.setParameter("from", range.from());
            query.setParameter("to", range.to());
        }

        return query.getSingleResult();
    }

    // ==============================
    // ✅ MORE EFFICIENT VERSION
    // Count once instead of filtering 4 times
    // ==============================

    public List<TimelinePointDto> getTimeline(
            LocalDate from,
            LocalDate to,
            UUID userId,
            boolean privileged
        ) {

        DateTimeRange range = DateUtils.toDateTimeRange(from, to);

        String jpql = """
            SELECT t.createdAt, t.status, t.dueDate
            FROM Task t
            %s
            AND t.createdAt >= :from
            AND t.createdAt < :to
        """;

        String where = privileged
            ? "WHERE 1=1"
            : "JOIN t.assignees u WHERE u.id = :userId";

        Query query = em.createQuery(jpql.formatted(where))
            .setParameter("from", range.from())
            .setParameter("to", range.to());

        if (!privileged) {
            query.setParameter("userId", userId);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        // Group and count in one pass
        Map<LocalDate, TimelineStats> statsMap = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (Object[] row : results) {
            LocalDateTime createdAt = (LocalDateTime) row[0];
            TaskStatus status = (TaskStatus) row[1];
            LocalDate dueDate = (LocalDate) row[2];

            LocalDate date = createdAt.toLocalDate();
            TimelineStats stats = statsMap.computeIfAbsent(date, k -> new TimelineStats());
            
            stats.created++;
            
            if (status == TaskStatus.COMPLETA) {
                stats.completed++;
            }
            if (status == TaskStatus.EM_PROGRESSO) {
                stats.inProgress++;
            }
            if (status != TaskStatus.COMPLETA && dueDate != null && dueDate.isBefore(today)) {
                stats.overdue++;
            }
        }

        // Convert to DTOs
        return statsMap.entrySet().stream()
            .map(entry -> new TimelinePointDto(
                entry.getKey(),
                entry.getValue().created,
                entry.getValue().completed,
                entry.getValue().inProgress,
                entry.getValue().overdue
            ))
            .sorted(Comparator.comparing(TimelinePointDto::date))
            .toList();
    }

    // Helper class
    private static class TimelineStats {
        long created = 0;
        long completed = 0;
        long inProgress = 0;
        long overdue = 0;
    }


}