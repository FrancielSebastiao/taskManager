package com.fransebastiao.taskmanager.dto.response;

public record LoginResponse(
    String accessToken,
    String refreshToken
) {}
