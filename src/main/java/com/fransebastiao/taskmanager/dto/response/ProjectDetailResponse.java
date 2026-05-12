package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectDetailResponse(
    UUID id,
    String name,
    String description,
    String status,
    String priority,
    String category,
    Integer progress,
    LocalDate startDate,
    LocalDate deadline,
    BigDecimal budget,
    BigDecimal spent,
    String iconBgClass,
    String iconColorClass,
    TaskBreakdownDto tasks,
    MemberAvatarDto manager,
    // Paginados separadamente
    PagedResponse<TaskSummaryDto> recentTasks,
    PagedResponse<ActivityDto> recentActivities,
    PagedResponse<TeamMemberDetailDto> teamMembers,
    PagedResponse<ProjectFileDto> files
) {}
