package com.fransebastiao.taskmanager.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.AssigneeAvatarDto;
import com.fransebastiao.taskmanager.dto.response.MemberAvatarDto;

@Component
public class AvatarHelper {

    // Paleta de cores — mesma que o frontend usa
    private static final List<String> COLORS = List.of(
        "#1D9E75", "#185FA5", "#854F0B",
        "#993C1D", "#534AB7", "#0F6E56",
        "#A32D2D", "#3B6D11", "#633806"
    );

    public String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";

        String[] parts = fullName.trim().split("\\s+");

        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";

        return (first + last).toUpperCase();
    }

    public String color(String name) {
        int index = Math.abs(name.hashCode()) % COLORS.size();
        return COLORS.get(index);
    }

    public MemberAvatarDto toMemberAvatar(User user) {
        return new MemberAvatarDto(
                user.getId(),
                user.getName(),
                initials(user.getName()),
                color(user.getName())
        );
    }

    public AssigneeAvatarDto toAssigneeAvatar(User user) {
        return new AssigneeAvatarDto(
                user.getId(),
                user.getName(),
                initials(user.getName()),
                color(user.getName())
        );
    }
}