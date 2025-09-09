package com.safework.api.domain.user.dto;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        Long organizationId,
        Long departmentId,
        LocalDateTime createdAt
) {}