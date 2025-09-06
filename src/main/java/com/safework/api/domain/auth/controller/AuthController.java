package com.safework.api.domain.auth.controller;

import com.safework.api.domain.auth.dto.LoginRequest;
import com.safework.api.domain.auth.dto.LoginResponse;
import com.safework.api.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return 401 for authentication failures
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}