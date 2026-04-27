package com.finflow.auth.dto;

public class AuthResponse {

    private String tokenType;
    private String accessToken;
    private long expiresIn;
    private String userId;
    private String email;
    private String displayName;

    public AuthResponse() {
    }

    public AuthResponse(String tokenType, String accessToken, long expiresIn, String userId, String email, String displayName) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
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
}
