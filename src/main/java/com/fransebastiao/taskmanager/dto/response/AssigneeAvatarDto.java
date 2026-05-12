package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record AssigneeAvatarDto(
    UUID   id,
    String name,
    String initials,
    String color
) {

}
