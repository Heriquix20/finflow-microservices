package com.finflow.auth.service;

import com.finflow.auth.model.AuthUser;
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
    private final long accessTokenTtlSeconds;

    public JwtTokenService(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${app.auth.access-token-ttl-seconds}") long accessTokenTtlSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public String generateToken(AuthUser user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenTtlSeconds);

        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
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

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }
}
