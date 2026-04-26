package com.finflow.gateway.auth.controller;

import com.finflow.gateway.auth.dto.AuthProfileResponse;
import com.finflow.gateway.auth.dto.AuthResponse;
import com.finflow.gateway.auth.dto.LoginRequest;
import com.finflow.gateway.auth.service.AuthService;
import com.finflow.gateway.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
@Import({AuthControllerTest.TestConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuthService authService;

    @Test
    void shouldLoginWithValidPayload() {
        when(authService.authenticate(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("Bearer", "token", 3600, "user-123", "demo", "Demo User"));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"username":"demo","password":"demo123"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("token")
                .jsonPath("$.userId").isEqualTo("user-123");
    }

    @Test
    void shouldReturnAuthenticatedProfile() {
        when(authService.getProfileFromAuthorizationHeader("Bearer token"))
                .thenReturn(new AuthProfileResponse("user-123", "demo", "Demo User"));

        webTestClient.get()
                .uri("/api/auth/me")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.displayName").isEqualTo("Demo User");
    }

    @Test
    void shouldReturnValidationProblemForBlankCredentials() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"username":"","password":""}
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Validation failed.")
                .jsonPath("$.errors.username").exists()
                .jsonPath("$.errors.password").exists();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        AuthService authService() {
            return mock(AuthService.class);
        }
    }
}
