package com.fransebastiao.taskmanager.dto.request;

import com.fransebastiao.taskmanager.validation.email.ValidEmail;
import com.fransebastiao.taskmanager.validation.password.PasswordMatches;
import com.fransebastiao.taskmanager.validation.password.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.With;

@With
@Builder
@PasswordMatches
public record UserRequest (
    @NotNull
    @NotBlank(message = "O Nome é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    String name,
    @NotNull
    @NotBlank(message = "O Email é obrigatório")
    @ValidEmail
    String email,
    @NotNull
    @NotBlank(message = "A Senha é obrigatória")
    @ValidPassword
    String password,
    @NotNull
    @NotBlank(message = "A Senha é obrigatória")
    String matchingPassword
    // @NotNull
    // @NotBlank(message = "O código é obrigatório")
    // String registrationCode
) {}