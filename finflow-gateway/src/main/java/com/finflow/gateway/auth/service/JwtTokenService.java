package com.finflow.gateway.auth.service;

import com.finflow.gateway.auth.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final AuthProperties authProperties;

    public JwtTokenService(
            @Value("${security.jwt.secret}") String jwtSecret,
            AuthProperties authProperties
    ) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.authProperties = authProperties;
    }

    public String generateToken(AuthProperties.User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(authProperties.getAccessTokenTtlSeconds());

        return Jwts.builder()
                .subject(user.getUserId())
                .claim("username", user.getUsername())
                .claim("displayName", user.getDisplayName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
