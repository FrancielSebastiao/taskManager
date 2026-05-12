package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

import com.fransebastiao.taskmanager.domain.user.User;

public record AssigneeDTO(UUID id, String name, String email) {
        public static AssigneeDTO from(User user) {
            return new AssigneeDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );
        }
    }