package com.fransebastiao.taskmanager.mapper;

import com.fransebastiao.taskmanager.domain.project.ProjectMember;
import com.fransebastiao.taskmanager.dto.response.ProjectMemberResponse;

public class ProjectMemberMapper {
    public static ProjectMemberResponse map(ProjectMember m) {
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
