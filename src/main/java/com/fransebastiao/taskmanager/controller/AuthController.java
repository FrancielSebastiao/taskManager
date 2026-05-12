package com.fransebastiao.taskmanager.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.request.LoginRequest;
import com.fransebastiao.taskmanager.dto.request.ResetPasswordRequest;
import com.fransebastiao.taskmanager.dto.request.UserRequest;
import com.fransebastiao.taskmanager.dto.response.LoginResponse;
import com.fransebastiao.taskmanager.dto.response.TokenRefreshResponse;
import com.fransebastiao.taskmanager.dto.response.UserDto;
import com.fransebastiao.taskmanager.exception.custom.InvalidTokenException;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.AuthService;
import com.fransebastiao.taskmanager.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshCookieName;

    @Value("${app.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${app.jwt.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie.path:/auth/refresh}")
    private String cookiePath;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRequest userRequest, @RequestParam("role_name") String roleName) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createAccount(userRequest, roleName));
    }

    @GetMapping("/resend-verification-token")
    public ResponseEntity<String> resend(@RequestParam String email) {
        userService.resendVerificationToken(email);
        return ResponseEntity.ok("Email de verificação Reenviado com sucesso");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verificar(@RequestParam String token) {
        userService.confirmAccount(token);
        return ResponseEntity.ok("Conta confirmada com sucesso!");
    }

    @GetMapping("/send-password-reset-token")
    public ResponseEntity<String> sendPasswordResetToken(@RequestParam String email) {
        userService.sendPasswordResetToken(email);
        return ResponseEntity.ok("Email da redefinição de senha enviado com sucesso");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
        @RequestParam String token,
        @RequestBody @Valid ResetPasswordRequest request
    ) {
        userService.resetPassword(token, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(response.refreshToken()).toString())
                .body(Map.of("accessToken", response.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @CookieValue(name = "${app.jwt.refresh-cookie-name}") String refreshToken) {

        TokenRefreshResponse response = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshCookie(response.rawRefreshToken()).toString())
                .body(Map.of("accessToken", response.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            @CookieValue(name = "${app.jwt.refresh-cookie-name}", required = false) String refreshToken) {

        String accessToken = extractBearerToken(request);
        authService.logout(accessToken, refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(new UserDto(userDetails.getId(), userDetails.getName(), userDetails.getEmail()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseCookie createRefreshCookie(String token) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path(cookiePath)
                .maxAge(refreshTokenExpiration / 1000)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path(cookiePath)   // ← tem de ser igual ao path de criação
                .maxAge(0)
                .build();
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidTokenException("Authorization header em falta");
        }
        return header.substring(7);
    }
}