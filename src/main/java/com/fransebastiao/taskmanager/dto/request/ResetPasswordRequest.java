package com.fransebastiao.taskmanager.dto.request;

import com.fransebastiao.taskmanager.validation.password.PasswordMatches;
import com.fransebastiao.taskmanager.validation.password.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@PasswordMatches
public record ResetPasswordRequest(
    @NotNull
    @NotBlank(message = "A Senha é obrigatória")
    @ValidPassword 
    String password,
    @NotNull
    @NotBlank(message = "A Senha é obrigatória")
    String matchingPassword
) {}
