package com.fransebastiao.taskmanager.service;

import com.fransebastiao.taskmanager.domain.user.VerificationToken;
import com.fransebastiao.taskmanager.domain.user.User;

public interface VerificationTokenService {
    String createVerificationTokenAndLink(User user);
    VerificationToken validateVerificationToken(String rawToken);
    void marcarComoUsado(VerificationToken token);
    boolean verify(String rawToken);
}
