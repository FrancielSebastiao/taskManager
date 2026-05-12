package com.fransebastiao.taskmanager.service;

import com.fransebastiao.taskmanager.domain.user.PasswordResetToken;
import com.fransebastiao.taskmanager.domain.user.User;

public interface PasswordResetTokenService {
    String createPasswordResetTokenAndLink(User user);
    PasswordResetToken validatePasswordResetToken(String rawToken);
    PasswordResetToken getToken(String rawToken);
}
