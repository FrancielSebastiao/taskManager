package com.fransebastiao.taskmanager.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fransebastiao.taskmanager.service.BlacklistService;
import com.fransebastiao.taskmanager.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final BlacklistService blacklistService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("Request to: {} | Auth header: {}", request.getServletPath(), authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found — skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                log.warn("Token failed validation");
                filterChain.doFilter(request, response);
                return;
            }

            String jti = jwtService.extractJti(token);
            if (blacklistService.isBlacklisted(jti)) {
                log.warn("Token is blacklisted — jti: {}", jti);
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractEmail(token);
            log.debug("Token valid for: {}", email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Authentication set for: {}", email);
            }

        } catch (JwtException e) {
            log.warn("JWT error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // Endpoints públicos ignoram o filtro
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/logout") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/resend-verification-token") ||
               path.startsWith("/api/v1/auth/verify") ||
               path.startsWith("/api/v1/auth/send-password-reset-token") ||
               path.startsWith("/api/v1/auth/reset-password") ||
               path.startsWith("/api/v1/auth/refresh");
    }
}