package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.user.RefreshToken;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.RefreshTokenResult;
import com.fransebastiao.taskmanager.exception.custom.ExpiredTokenException;
import com.fransebastiao.taskmanager.exception.custom.InvalidTokenException;
import com.fransebastiao.taskmanager.repository.RefreshTokenRepository;
import com.fransebastiao.taskmanager.service.JwtService;
import com.fransebastiao.taskmanager.service.RefreshTokenService;
import com.fransebastiao.taskmanager.util.GenericHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Override
    public RefreshTokenResult create(User user) {
        String rawToken   = jwtService.createRefreshToken(user);
        String hashedToken = GenericHelper.hashToken(rawToken);

        LocalDateTime exp = LocalDateTime.now()
                .plusSeconds(refreshTokenExpiration / 1000);

        RefreshToken refreshToken = new RefreshToken(user, hashedToken, exp);
        refreshTokenRepository.save(refreshToken);

        return new RefreshTokenResult(refreshToken, rawToken); // devolve ambos
    }

    @Transactional(readOnly = true)
    @Override
    public RefreshToken validate(String token) {
        String hashedToken = GenericHelper.hashToken(token);
        RefreshToken refreshToken = refreshTokenRepository.findByHashedToken(hashedToken).orElseThrow(() -> new InvalidTokenException("Invalid Refresh token"));

        if (refreshToken.isRevoked()) {
            // Possível reutilização — revogar todos os tokens do utilizador
            refreshTokenRepository.revokeAllPerUser(refreshToken.getUser());
            log.warn("Revoked Refresh token reutilized for user: {}", refreshToken.getUser().getEmail());
            throw new InvalidTokenException("Revoked Refresh token — Session ended for security reasons");
        }

        if (refreshToken.isExpired()) {
            throw new ExpiredTokenException("Refresh token is expired — login again");
        }

        return refreshToken;
    }

    @Override
    public void revokeAllPerUser(User user) {
        refreshTokenRepository.revokeAllPerUser(user);
    }
}
