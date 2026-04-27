package com.finflow.auth.service;

import com.finflow.auth.dto.AuthProfileResponse;
import com.finflow.auth.dto.AuthResponse;
import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.RegisterRequest;
import com.finflow.auth.model.AuthUser;
import com.finflow.auth.repository.AuthUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthUserRepository authUserRepository,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (authUserRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
        }

        AuthUser user = new AuthUser();
        user.setEmail(normalizedEmail);
        user.setDisplayName(request.getDisplayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        AuthUser savedUser = authUserRepository.save(user);
        return toAuthResponse(savedUser);
    }

    public AuthResponse authenticate(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        AuthUser user = authUserRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return toAuthResponse(user);
    }

    public AuthProfileResponse getProfile(String userId) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user was not found."));

        return new AuthProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt()
        );
    }

    public AuthProfileResponse getProfileFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing or invalid.");
        }

        String token = authorizationHeader.substring("Bearer ".length());
        String userId = jwtTokenService.extractUserId(token);
        return getProfile(userId);
    }

    private AuthResponse toAuthResponse(AuthUser user) {
        return new AuthResponse(
                "Bearer",
                jwtTokenService.generateToken(user),
                jwtTokenService.getAccessTokenTtlSeconds(),
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
