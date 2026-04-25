package com.finflow.gateway.auth.service;

import com.finflow.gateway.auth.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    @Test
    void shouldGenerateTokenWithConfiguredClaims() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setAccessTokenTtlSeconds(3600);

        AuthProperties.User user = new AuthProperties.User();
        user.setUserId("user-123");
        user.setUsername("demo");
        user.setPassword("demo123");
        user.setDisplayName("Demo User");

        JwtTokenService jwtTokenService = new JwtTokenService(
                "minha-chave-super-secreta-minha-chave-super-secreta",
                authProperties
        );

        String token = jwtTokenService.generateToken(user);
        SecretKey key = Keys.hmacShaKeyFor("minha-chave-super-secreta-minha-chave-super-secreta".getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("user-123");
        assertThat(claims.get("username", String.class)).isEqualTo("demo");
        assertThat(claims.get("displayName", String.class)).isEqualTo("Demo User");
        assertThat(jwtTokenService.extractUserId(token)).isEqualTo("user-123");
    }
}
