package com.fransebastiao.taskmanager.mapper;

import com.fransebastiao.taskmanager.domain.project.ProjectCategory;
import com.fransebastiao.taskmanager.domain.project.ProjectMemberRole;
import com.fransebastiao.taskmanager.domain.task.TaskCategory;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;

public class NameAndDescriptionMapper {

    public static NameAndDescriptionResponse mapToProjectCategory(ProjectCategory c) {
        return new NameAndDescriptionResponse(
            c.getId(), c.getName(), c.getDescription()
        );
    }

    public static NameAndDescriptionResponse mapToTaskCategory(TaskCategory c) {
        return new NameAndDescriptionResponse(
            c.getId(), c.getName(), c.getDescription()
        );
    }

    public static NameAndDescriptionResponse mapToMemberRole(ProjectMemberRole r) {
        return new NameAndDescriptionResponse(
            r.getId(), r.getName(), r.getDescription()
        );
    }
}
