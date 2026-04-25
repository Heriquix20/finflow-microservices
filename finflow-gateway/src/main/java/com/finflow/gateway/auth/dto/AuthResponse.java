package com.finflow.gateway.auth.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        String userId,
        String username,
        String displayName
) {
}
