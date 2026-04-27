package com.finflow.auth.controller;

import com.finflow.auth.dto.AuthProfileResponse;
import com.finflow.auth.dto.AuthResponse;
import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.RegisterRequest;
import com.finflow.auth.handler.GlobalExceptionHandler;
import com.finflow.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(buildAuthResponse());

        String payload = """
                {
                  "displayName": "New User",
                  "email": "new@example.com",
                  "password": "strongpass"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void loginShouldReturnOk() throws Exception {
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(buildAuthResponse());

        String payload = """
                {
                  "email": "user@example.com",
                  "password": "strongpass"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));
    }

    @Test
    void loginShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String payload = """
                {
                  "email": "invalid",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed."));
    }

    @Test
    void meShouldReturnAuthenticatedProfile() throws Exception {
        when(authService.getProfileFromAuthorizationHeader("Bearer jwt-token")).thenReturn(
                new AuthProfileResponse("user-123", "user@example.com", "User Example", LocalDateTime.of(2026, 4, 27, 8, 0))
        );

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    private AuthResponse buildAuthResponse() {
        return new AuthResponse(
                "Bearer",
                "jwt-token",
                3600,
                "user-123",
                "user@example.com",
                "User Example"
        );
    }
}
