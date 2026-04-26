package com.finflow.gateway.auth.service;

import com.finflow.gateway.auth.config.AuthProperties;
import com.finflow.gateway.auth.dto.AuthProfileResponse;
import com.finflow.gateway.auth.dto.AuthResponse;
import com.finflow.gateway.auth.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private JwtTokenService jwtTokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtTokenService = mock(JwtTokenService.class);
        when(jwtTokenService.generateToken(any(AuthProperties.User.class))).thenReturn("jwt-token");

        AuthProperties authProperties = new AuthProperties();
        authProperties.setAccessTokenTtlSeconds(3600);

        AuthProperties.User demoUser = new AuthProperties.User();
        demoUser.setUserId("user-123");
        demoUser.setUsername("demo");
        demoUser.setPassword("demo123");
        demoUser.setDisplayName("Demo User");
        authProperties.setUsers(List.of(demoUser));

        authService = new AuthService(jwtTokenService, authProperties);
    }

    @Test
    void shouldAuthenticateConfiguredUser() {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("demo123");

        AuthResponse response = authService.authenticate(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo("user-123");
        assertThat(response.username()).isEqualTo("demo");
    }

    @Test
    void shouldRejectInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("wrong");

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }

    @Test
    void shouldReturnProfileForAuthenticatedUser() {
        AuthProfileResponse response = authService.getProfile("user-123");

        assertThat(response.userId()).isEqualTo("user-123");
        assertThat(response.displayName()).isEqualTo("Demo User");
    }

    @Test
    void shouldResolveProfileFromBearerToken() {
        when(jwtTokenService.extractUserId("jwt-token")).thenReturn("user-123");

        AuthProfileResponse response = authService.getProfileFromAuthorizationHeader("Bearer jwt-token");

        assertThat(response.username()).isEqualTo("demo");
    }
}
