package com.fransebastiao.taskmanager.service;

import java.util.Date;

import com.fransebastiao.taskmanager.domain.user.User;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String createAccessToken(User user);
    String createRefreshToken(User user);
    Claims extractClaims(String token);
    String extractEmail(String token);
    String extractJti(String token);
    Date extractExpiration(String token);
    boolean isTokenValid(String token);
    long getRefreshTokenExpiration();
}
