package com.finflow.auth.service;

import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.RegisterRequest;
import com.finflow.auth.model.AuthUser;
import com.finflow.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Spy
    private JwtTokenService jwtTokenService = new JwtTokenService(
            "minha-chave-super-secreta-minha-chave-super-secreta",
            3600
    );

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthService authService;

    private AuthUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new AuthUser(
                "user-123",
                "user@example.com",
                "User Example",
                passwordEncoder.encode("strongpass"),
                LocalDateTime.of(2026, 4, 27, 8, 0)
        );
    }

    @Test
    void registerShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest("New User", "NewUser@Example.com", "strongpass");
        when(authUserRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(authUserRepository.save(any(AuthUser.class))).thenAnswer(invocation -> {
            AuthUser user = invocation.getArgument(0);
            user.setId("generated-id");
            return user;
        });

        var response = authService.register(request);

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        verify(authUserRepository).save(captor.capture());
        AuthUser savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getDisplayName()).isEqualTo("New User");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("strongpass");
        assertThat(response.getUserId()).isEqualTo("generated-id");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getAccessToken()).isNotBlank();
    }

    @Test
    void registerShouldRejectDuplicatedEmail() {
        when(authUserRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("User", "user@example.com", "strongpass")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void authenticateShouldReturnTokenForValidCredentials() {
        when(authUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        var response = authService.authenticate(new LoginRequest("user@example.com", "strongpass"));

        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getAccessToken()).isNotBlank();
    }

    @Test
    void authenticateShouldFailForWrongPassword() {
        when(authUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.authenticate(new LoginRequest("user@example.com", "wrongpass")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }

    @Test
    void authenticateShouldFailForUnknownEmail() {
        when(authUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(new LoginRequest("missing@example.com", "strongpass")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }

    @Test
    void getProfileShouldReturnUserData() {
        when(authUserRepository.findById("user-123")).thenReturn(Optional.of(existingUser));

        var response = authService.getProfile("user-123");

        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void getProfileFromAuthorizationHeaderShouldFailWhenHeaderIsMissing() {
        assertThatThrownBy(() -> authService.getProfileFromAuthorizationHeader(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401 UNAUTHORIZED");
    }
}
