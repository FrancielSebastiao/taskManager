package com.fransebastiao.taskmanager.task;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.repository.BlacklistedTokenRepository;
import com.fransebastiao.taskmanager.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final RefreshTokenRepository     refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Scheduled(cron = "0 0 3 * * *") // todos os dias às 3h
    @Transactional
    public void limpar() {
        int refreshRemovidos     = refreshTokenRepository.deleteAllExpiredOrRevoked(LocalDateTime.now());
        int blacklistRemovidos   = blacklistedTokenRepository.deleteAllExpired(LocalDateTime.now());
        log.info("Limpeza de tokens — refresh: {}, blacklist: {}", refreshRemovidos, blacklistRemovidos);
    }
}
