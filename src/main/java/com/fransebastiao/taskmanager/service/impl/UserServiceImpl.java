package com.fransebastiao.taskmanager.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.user.PasswordResetToken;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.domain.user.VerificationToken;
import com.fransebastiao.taskmanager.dto.request.ResetPasswordRequest;
import com.fransebastiao.taskmanager.dto.request.UpdatePasswordRequest;
import com.fransebastiao.taskmanager.dto.request.UserRequest;
import com.fransebastiao.taskmanager.dto.response.UserDto;
import com.fransebastiao.taskmanager.exception.custom.EmailAlreadyExistsException;
import com.fransebastiao.taskmanager.exception.custom.InvalidPasswordException;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.repository.RoleRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.EmailService;
import com.fransebastiao.taskmanager.service.PasswordResetTokenService;
import com.fransebastiao.taskmanager.service.UserService;
import com.fransebastiao.taskmanager.service.VerificationTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenService passwordResetTokenService;

    @Transactional
    @Override
    public UserDto createAccount(UserRequest request, String roleName) {
        if (emailExists(request.email())) {
            throw new EmailAlreadyExistsException("Email: " + request.email() + " já existe");
        }

        // if (request.registrationCode() != "@NIC2026__") {
        //     throw new IllegalArgumentException("Código Inválido");
        // }

        User user = new User(
            request.name(),
            request.email(),
            passwordEncoder.encode(request.password())
        );

        user.setRoles(Set.of(roleRepository.findByName(roleName).orElseThrow(() -> new ResourceNotFoundException("Role not found"))));

        User saved = userRepository.save(user);

        String link = verificationTokenService.createVerificationTokenAndLink(saved);
        emailService.enviarEmailVerificacao(saved.getEmail(), saved.getName(), link);

        log.info("Novo utilizador registado: {}", saved.getEmail());
        return UserDto.from(saved);
    }

    @Override
    public void resendVerificationToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        if (user.isActive()) { return; }

        String link = verificationTokenService.createVerificationTokenAndLink(user);
        emailService.enviarEmailVerificacao(user.getEmail(), user.getName(), link);
    }

    @Transactional
    @Override
    public void confirmAccount(String token) {
        VerificationToken verificationToken = verificationTokenService.validateVerificationToken(token);

        User user = verificationToken.getUser();
        user.activate();
        verificationTokenService.marcarComoUsado(verificationToken);

        log.info("Conta confirmada para: {}", user.getEmail());
    }

    @Override
    public void sendPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        String link = passwordResetTokenService.createPasswordResetTokenAndLink(user);
        emailService.enviarEmailPasswordReset(user.getEmail(), user.getName(), link);

        log.info("Email enviado para: {}", user.getEmail());
    }

    @Transactional
    @Override
    public void resetPassword(String rawToken, ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenService.getToken(rawToken);

        User user = prt.getUser();
        if (user.getPasswordHash() == request.password()) {
            throw new InvalidPasswordException("Nova senha não pode ser a mesma");
        }
            
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updatePassword(User user, UpdatePasswordRequest updatePasswordRequest) {
        if (user.getPasswordHash() == updatePasswordRequest.password()) {
            throw new InvalidPasswordException("Nova senha não pode ser a mesma");
        }

        user.updatePassword(updatePasswordRequest.password());
        userRepository.save(user);
    }

    // public UserDto updateAccount(String email, ) {
    //     User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    //     user.setName();
    // }

    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return userRepository.findUsers();
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
