package com.safework.api.domain.auth.dto;

import com.safework.api.domain.user.model.UserRole;

public record LoginResponse(
        String token,
        String type,
        Long id,
        String email,
        String name,
        UserRole role,
        Long organizationId
) {
    // Constructor with default type value
    public LoginResponse(String token, Long id, String email, String name, 
                        UserRole role, Long organizationId) {
        this(token, "Bearer", id, email, name, role, organizationId);
    }
}