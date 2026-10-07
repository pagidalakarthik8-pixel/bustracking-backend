package com.bustrack.controller;

import com.bustrack.dto.Dtos.*;
import com.bustrack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest req) {

        return authService.register(req);
    }

    // Login
    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest req) {

        return authService.login(req);
    }

    // Get current user
    @GetMapping("/me")
    public UserResponse me(Authentication auth) {

        return UserResponse.from(
                authService.currentUser(auth.getName())
        );
    }

    // Update current user
    @PutMapping("/me")
    public UserResponse updateMe(
            Authentication auth,
            @Valid @RequestBody ProfileRequest req) {

        return authService.updateProfile(
                auth.getName(),
                req
        );
    }
}