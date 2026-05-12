package com.fransebastiao.taskmanager.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.user.RefreshToken;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.LoginRequest;
import com.fransebastiao.taskmanager.dto.response.LoginResponse;
import com.fransebastiao.taskmanager.dto.response.RefreshTokenResult;
import com.fransebastiao.taskmanager.dto.response.TokenRefreshResponse;
import com.fransebastiao.taskmanager.exception.custom.AccountNotVerifiedException;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.AuthService;
import com.fransebastiao.taskmanager.service.BlacklistService;
import com.fransebastiao.taskmanager.service.JwtService;
import com.fransebastiao.taskmanager.service.RefreshTokenService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authManager;
    private final BlacklistService blacklistService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new AccountNotVerifiedException("Account not confirmed — check your email");
        }

        String             accessToken       = jwtService.createAccessToken(user);
        RefreshTokenResult refreshTokenResult = refreshTokenService.create(user);

        log.info("Login efectuado: {}", user.getEmail());
        return new LoginResponse(accessToken, refreshTokenResult.rawToken()); // raw token para o cookie
    }

    @Override
    public TokenRefreshResponse refresh(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.validate(refreshTokenStr);

        refreshToken.revoke();

        RefreshTokenResult newRefreshToken = refreshTokenService.create(refreshToken.getUser());
        String newAccessToken = jwtService.createAccessToken(refreshToken.getUser());

        log.info("Tokens renovados para: {}", refreshToken.getUser().getEmail());
        return new TokenRefreshResponse(newAccessToken, newRefreshToken.rawToken()); // raw token para o cookie
    }

    @Override
    public void logout(String accessToken, String refreshTokenStr) {

        if (refreshTokenStr != null) {
            try {
                // Blacklistar o access token
                blacklistService.blacklist(accessToken, jwtService.extractExpiration(accessToken));

                // Revogar o refresh token
                RefreshToken refreshToken = refreshTokenService.validate(refreshTokenStr);
                refreshToken.revoke();

                log.info("Logout efectuado");
            } catch(Exception e) {
                log.warn("Refresh token invalido durante logout");
            }
        }
    }

    @Override
    public void logoutDetodos(String email, String accessToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilizador não encontrado"));

        blacklistService.blacklist(accessToken, jwtService.extractExpiration(accessToken));
        refreshTokenService.revokeAllPerUser(user);

        log.info("Logout de todos os dispositivos: {}", email);
    }
}
