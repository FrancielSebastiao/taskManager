package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.user.PasswordResetToken;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.exception.custom.ExpiredTokenException;
import com.fransebastiao.taskmanager.exception.custom.InvalidTokenException;
import com.fransebastiao.taskmanager.repository.PasswordResetTokenRepository;
import com.fransebastiao.taskmanager.service.PasswordResetTokenService;
import com.fransebastiao.taskmanager.util.GenericHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    @Override
    public String createPasswordResetTokenAndLink(User user) {
        passwordResetTokenRepository.deleteByUser(user);

        String rawToken = GenericHelper.generateToken();
        String hashedToken = GenericHelper.hashToken(rawToken);
        
        PasswordResetToken passwordResetToken = new PasswordResetToken(
            hashedToken,
            user,
            LocalDateTime.now().plusMinutes(15),
            null
        );
        
        passwordResetTokenRepository.save(passwordResetToken);
        return baseUrl + "/auth/resend?token=" + rawToken;
    }

    @Transactional(readOnly = true)
    @Override
    public PasswordResetToken validatePasswordResetToken(String rawToken) {
        String hashedToken = GenericHelper.hashToken(rawToken);
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByHashedToken(hashedToken).orElseThrow(() -> new InvalidTokenException("Token inválido"));

        if (passwordResetToken.isUsed() && passwordResetToken.getUsedAt() != null) {
            throw new InvalidTokenException("Token já foi utilizado");
        }

        if (passwordResetToken.isExpired()) {
            throw new ExpiredTokenException("Token expirado — solicite um novo");
        }

        return passwordResetToken;
    }

    @Transactional(readOnly = true)
    public PasswordResetToken getToken(String rawToken) {
        String hashedToken = GenericHelper.hashToken(rawToken);
        return passwordResetTokenRepository.findByHashedToken(hashedToken).orElseThrow(() -> new InvalidTokenException("Token inválido"));
    }
}
