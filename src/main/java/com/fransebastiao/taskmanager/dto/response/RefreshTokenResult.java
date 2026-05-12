package com.fransebastiao.taskmanager.dto.response;

import com.fransebastiao.taskmanager.domain.user.RefreshToken;

public record RefreshTokenResult(
    RefreshToken entity,
    String rawToken
) {}
