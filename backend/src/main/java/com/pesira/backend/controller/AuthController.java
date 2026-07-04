package com.pesira.backend.controller;

import com.pesira.backend.dto.ApiResponse;
import com.pesira.backend.dto.PageResponse;
import com.pesira.backend.dto.auth.AuthResponse;
import com.pesira.backend.dto.auth.LoginRequest;
import com.pesira.backend.dto.auth.RegisterRequest;
import com.pesira.backend.dto.auth.UserResponse;
import com.pesira.backend.security.UserPrincipal;
import com.pesira.backend.service.AuthService;
import com.pesira.backend.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.AUTH_API)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, registration, and session endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponse user = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved", user));
    }
}
