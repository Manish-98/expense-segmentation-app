package com.expense.segmentation.controller;

import com.expense.segmentation.dto.AuthResponse;
import com.expense.segmentation.dto.LoginRequest;
import com.expense.segmentation.dto.RegisterRequest;
import com.expense.segmentation.dto.UserResponse;
import com.expense.segmentation.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - Registration request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("POST /auth/register - User registered successfully: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("POST /auth/login - User logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.debug("GET /auth/me - Fetching current user details");
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
