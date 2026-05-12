package com.fransebastiao.taskmanager.service;

import java.util.List;

import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.ResetPasswordRequest;
import com.fransebastiao.taskmanager.dto.request.UpdatePasswordRequest;
import com.fransebastiao.taskmanager.dto.request.UserRequest;
import com.fransebastiao.taskmanager.dto.response.UserDto;

public interface UserService {
    UserDto createAccount(UserRequest request, String roleName);
    void resendVerificationToken(String email);
    void confirmAccount(String token);
    void sendPasswordResetToken(String email);
    void resetPassword(String rawToken, ResetPasswordRequest request);
    void updatePassword(User user, UpdatePasswordRequest updatePasswordRequest);
    List<UserDto> getUsers();
}
