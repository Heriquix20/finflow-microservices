package com.finflow.gateway.auth.dto;

public record AuthProfileResponse(
        String userId,
        String username,
        String displayName
) {
}
