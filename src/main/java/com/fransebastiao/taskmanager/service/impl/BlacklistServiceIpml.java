package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.user.BlacklistedToken;
import com.fransebastiao.taskmanager.repository.BlacklistedTokenRepository;
import com.fransebastiao.taskmanager.service.BlacklistService;
import com.fransebastiao.taskmanager.service.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BlacklistServiceIpml implements BlacklistService {
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    public void blacklist(String token, Date expiration) {
        String jti = jwtService.extractJti(token);
        LocalDateTime expiresAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        blacklistedTokenRepository.save(new BlacklistedToken(jti, expiresAt));
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return blacklistedTokenRepository.existsByJti(jti);
    }
}
