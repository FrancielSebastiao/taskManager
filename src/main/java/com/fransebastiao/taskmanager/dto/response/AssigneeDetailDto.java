package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record AssigneeDetailDto(
    UUID id,
    String initials,
    String name,
    String role, // ProjectMember.MemberRole
    String color,
    String email
) {}