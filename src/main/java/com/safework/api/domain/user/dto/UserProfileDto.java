package com.safework.api.domain.user.dto;

import java.time.LocalDateTime;

public record UserProfileDto(
        Long id,
        String name,
        String email,
        String role,
        String organizationName,
        String departmentName,
        LocalDateTime createdAt
) {}