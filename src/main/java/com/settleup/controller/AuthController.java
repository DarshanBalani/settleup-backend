package com.settleup.controller;

import com.settleup.dto.auth.AuthResponse;
import com.settleup.dto.auth.LoginRequest;
import com.settleup.dto.auth.RefreshTokenRequest;
import com.settleup.dto.auth.RegisterRequest;
import com.settleup.dto.common.ApiResponseDto;
import com.settleup.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Public registration creates a USER account. Admin accounts must be seeded via Flyway.")
    public ResponseEntity<ApiResponseDto<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials", description = "Validates credentials and returns short-lived JWT access token (~15m) and refresh token.")
    public ResponseEntity<ApiResponseDto<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token", description = "Exchanges a valid refresh token for a new access token and refresh token pair.")
    public ResponseEntity<ApiResponseDto<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Token refreshed successfully"));
    }
}
