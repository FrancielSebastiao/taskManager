package com.fransebastiao.taskmanager.service;

import com.fransebastiao.taskmanager.domain.user.RefreshToken;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.RefreshTokenResult;

public interface RefreshTokenService {
    RefreshTokenResult create(User user);
    RefreshToken validate(String token);
    void revokeAllPerUser(User user);
}
