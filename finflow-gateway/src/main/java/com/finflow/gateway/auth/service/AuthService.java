package com.finflow.gateway.auth.service;

import com.finflow.gateway.auth.config.AuthProperties;
import com.finflow.gateway.auth.dto.AuthProfileResponse;
import com.finflow.gateway.auth.dto.AuthResponse;
import com.finflow.gateway.auth.dto.LoginRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final JwtTokenService jwtTokenService;
    private final AuthProperties authProperties;
    private final Map<String, AuthProperties.User> usersByUsername;
    private final Map<String, AuthProperties.User> usersByUserId;

    public AuthService(JwtTokenService jwtTokenService, AuthProperties authProperties) {
        this.jwtTokenService = jwtTokenService;
        this.authProperties = authProperties;
        this.usersByUsername = authProperties.getUsers().stream()
                .collect(Collectors.toUnmodifiableMap(AuthProperties.User::getUsername, Function.identity()));
        this.usersByUserId = authProperties.getUsers().stream()
                .collect(Collectors.toUnmodifiableMap(AuthProperties.User::getUserId, Function.identity()));
    }

    public AuthResponse authenticate(LoginRequest request) {
        AuthProperties.User user = usersByUsername.get(request.getUsername());

        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        return new AuthResponse(
                "Bearer",
                jwtTokenService.generateToken(user),
                authProperties.getAccessTokenTtlSeconds(),
                user.getUserId(),
                user.getUsername(),
                user.getDisplayName()
        );
    }

    public AuthProfileResponse getProfile(String userId) {
        AuthProperties.User user = usersByUserId.get(userId);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user was not found.");
        }

        return new AuthProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getDisplayName()
        );
    }

    public AuthProfileResponse getProfileFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token ausente ou invalido.");
        }

        String token = authorizationHeader.substring("Bearer ".length());
        String userId = jwtTokenService.extractUserId(token);

        return getProfile(userId);
    }
}
