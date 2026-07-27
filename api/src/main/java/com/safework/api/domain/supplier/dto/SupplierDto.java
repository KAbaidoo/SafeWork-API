package com.safework.api.domain.supplier.dto;

import java.time.LocalDateTime;

public record SupplierDto(
        Long id,
        String name,
        String contactPerson,
        String email,
        String phoneNumber,
        String address,
        Long organizationId,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}