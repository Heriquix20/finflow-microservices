package com.finflow.gateway.auth.controller;

import com.finflow.gateway.auth.dto.AuthProfileResponse;
import com.finflow.gateway.auth.dto.AuthResponse;
import com.finflow.gateway.auth.dto.LoginRequest;
import com.finflow.gateway.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.authenticate(request);
    }

    @GetMapping("/me")
    public AuthProfileResponse me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return authService.getProfileFromAuthorizationHeader(authorizationHeader);
    }
}
