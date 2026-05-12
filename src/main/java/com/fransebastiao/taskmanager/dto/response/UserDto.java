package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

import com.fransebastiao.taskmanager.domain.user.User;

public record UserDto(
    UUID id,
    String name,
    String email
) {
    public static UserDto from(User u) {
        return new UserDto(
            u.getId(),
            u.getName(),
            u.getEmail()
        );
    }
}