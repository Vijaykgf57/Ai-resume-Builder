package com.resumeai.controller;

import com.resumeai.dto.AuthResponse;
import com.resumeai.dto.LoginRequest;
import com.resumeai.dto.RegisterRequest;
import com.resumeai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Register a normal user */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Register an admin account.
     * Protected by a secret key header so it can't be called by anyone publicly.
     * Header: X-Admin-Secret: resumeai-admin-2024
     */
    @PostMapping("/register-admin")
    public ResponseEntity<AuthResponse> registerAdmin(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader("X-Admin-Secret") String secret
    ) {
        if (!"resumeai-admin-2024".equals(secret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
