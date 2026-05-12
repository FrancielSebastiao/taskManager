package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.project.ProjectMember;

public record ProjectMemberResponse(
    UUID id, 
    UUID projectId,
    String projectName,
    UUID userId,
    String userName,
    String userEmail,
    String roleName,
    LocalDateTime joinedAt
) {
    public static ProjectMemberResponse from(ProjectMember m) {
        return new ProjectMemberResponse(
            m.getId(), 
            m.getProject().getId(),
            m.getProject().getName(),
            m.getUser().getId(),
            m.getUser().getName(), 
            m.getUser().getEmail(),
            m.getRole().getName(),
            m.getJoinedAt()
        );
    }
}
