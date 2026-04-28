package com.finflow.auth.dto;

import java.time.LocalDateTime;

public class AuthProfileResponse {

    private String userId;
    private String email;
    private String displayName;
    private LocalDateTime createdAt;

    public AuthProfileResponse() {
    }

    public AuthProfileResponse(String userId, String email, String displayName, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
