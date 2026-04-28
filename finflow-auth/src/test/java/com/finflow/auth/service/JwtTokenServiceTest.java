package com.finflow.auth.service;

import com.finflow.auth.model.AuthUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private static final String SECRET = "minha-chave-super-secreta-minha-chave-super-secreta";

    @Test
    void shouldGenerateTokenAndExtractUserId() {
        JwtTokenService service = new JwtTokenService(SECRET, 3600);
        AuthUser user = new AuthUser("user-123", "user@example.com", "User Example", "hash", LocalDateTime.now());

        String token = service.generateToken(user);

        assertThat(service.extractUserId(token)).isEqualTo("user-123");
        assertThat(service.getAccessTokenTtlSeconds()).isEqualTo(3600);
    }
}
