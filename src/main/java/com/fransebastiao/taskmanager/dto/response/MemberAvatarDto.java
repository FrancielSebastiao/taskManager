package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;

public record MemberAvatarDto(
    UUID   id,
    String name,
    String initials,  // "SC" para Sara Chen
    String color      // cor gerada a partir do id
) {}
