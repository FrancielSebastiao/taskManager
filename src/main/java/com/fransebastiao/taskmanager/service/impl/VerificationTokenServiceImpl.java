package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.domain.user.VerificationToken;
import com.fransebastiao.taskmanager.exception.custom.ExpiredTokenException;
import com.fransebastiao.taskmanager.exception.custom.InvalidTokenException;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.repository.VerificationTokenRepository;
import com.fransebastiao.taskmanager.service.VerificationTokenService;
import com.fransebastiao.taskmanager.util.GenericHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    @Override
    public String createVerificationTokenAndLink(User user) {
        verificationTokenRepository.deleteByUser(user);

        String rawToken = GenericHelper.generateToken();
        String hashedToken = GenericHelper.hashToken(rawToken);
        
        VerificationToken verificationToken = new VerificationToken(
            hashedToken,
            user,
            LocalDateTime.now().plusHours(24),
            null
        );
            
        verificationTokenRepository.save(verificationToken);
        return baseUrl + "/auth/verify?token=" + rawToken;
    }

    @Transactional(readOnly = true)
    @Override
    public VerificationToken validateVerificationToken(String rawToken) {
        String hashedToken = GenericHelper.hashToken(rawToken);
        VerificationToken verificationToken = verificationTokenRepository.findByHashedToken(hashedToken).orElseThrow(() -> new InvalidTokenException("Token inválido"));

        if (verificationToken.isUsed() && verificationToken.getUsedAt() != null) {
            throw new InvalidTokenException("Token já foi utilizado");
        }

        if (verificationToken.isExpired()) {
            throw new ExpiredTokenException("Token expirado — solicite um novo");
        }

        return verificationToken;
    }

    @Transactional
    @Override
    public void marcarComoUsado(VerificationToken token) {
        token.markAsUsed();
    }

    @Transactional
    @Override
    public boolean verify(String rawToken) {
        String hashedToken = GenericHelper.hashToken(rawToken);
        VerificationToken verificationToken = verificationTokenRepository.findByHashedToken(hashedToken).orElseThrow(() -> new ResourceNotFoundException("Token not found."));
        if (verificationToken != null) {
            User user = verificationToken.getUser();
            if (!user.isActive()) {
                user.activate();
                userRepository.save(user);
                verificationToken.markAsUsed();
                verificationToken.setUsedAt(LocalDateTime.now());
                verificationTokenRepository.save(verificationToken);
                return true;
            }
        }
        return false;
    }
}