package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record TeamMemberDetailDto(
UUID id,
String initials,
String name,
String role,
String color
) {}
