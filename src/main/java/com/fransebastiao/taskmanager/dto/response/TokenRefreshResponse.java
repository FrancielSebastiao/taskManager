package com.fransebastiao.taskmanager.dto.response;

public record TokenRefreshResponse(
    String accessToken,
    String rawRefreshToken
) {}
