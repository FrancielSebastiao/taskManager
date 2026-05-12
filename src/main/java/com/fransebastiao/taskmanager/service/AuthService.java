package com.fransebastiao.taskmanager.service;

import com.fransebastiao.taskmanager.dto.request.LoginRequest;
import com.fransebastiao.taskmanager.dto.response.LoginResponse;
import com.fransebastiao.taskmanager.dto.response.TokenRefreshResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    TokenRefreshResponse refresh(String refreshTokenStr);
    void logout(String accessToken, String refreshTokenStr);
    void logoutDetodos(String email, String accessToken);
}
