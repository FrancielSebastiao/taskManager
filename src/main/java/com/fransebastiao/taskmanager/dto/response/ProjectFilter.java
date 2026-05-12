package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.project.Project;

public record ProjectFilter(
    List<Project.ProjectStatus> statuses,
    List<Project.Priority>      priorities,
    String                      category,
    UUID                        managerId,
    LocalDate                   deadlineFrom,
    LocalDate                   deadlineTo,
    Boolean                     myProjectsOnly,  // ← NOVO: null = all projects, true = my projects only
    int                         page,
    int                         size,
    String                      sortBy,
    String                      sortDir,
    String                      search
) {
    public ProjectFilter {
        page    = page    <= 0 ? 0  : page;
        size    = size    <= 0 ? 20 : Math.min(size, 100);
        sortBy  = sortBy  == null ? "createdAt" : sortBy;
        sortDir = sortDir == null ? "desc"      : sortDir;
    }
}