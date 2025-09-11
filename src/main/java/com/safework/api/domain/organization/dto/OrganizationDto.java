package com.safework.api.domain.organization.dto;

import java.time.LocalDateTime;

public record OrganizationDto(
        Long id,
        String name,
        String address,
        String phone,
        String website,
        String industry,
        String size,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}