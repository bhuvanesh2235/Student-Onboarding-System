package com.bhuvanesh.onboarding.student_service.auth.controller;

import com.bhuvanesh.onboarding.student_service.auth.dto.AuthResponse;
import com.bhuvanesh.onboarding.student_service.auth.dto.LoginRequest;
import com.bhuvanesh.onboarding.student_service.auth.dto.SignupRequest;
import com.bhuvanesh.onboarding.student_service.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for user signup and login")
public class AuthController {

    private final AuthService authService;

    // ─── POST /auth/signup ────────────────────────────────────────────────────
    @PostMapping("/signup")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new account. Returns 409 if email is already registered.")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request) {

        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─── POST /auth/login ─────────────────────────────────────────────────────
    @PostMapping("/login")
    @Operation(
        summary = "Login",
        description = "Verifies email + BCrypt password. Returns user info on success.")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
