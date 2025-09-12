package com.safework.api.domain.supplier.dto;

public record SupplierSummaryDto(
        Long id,
        String name,
        String contactPerson,
        String email,
        String phoneNumber
) {}