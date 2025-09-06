package com.safework.api.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// A concise, immutable, and validated DTO.
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}