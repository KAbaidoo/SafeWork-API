package com.safework.api.domain.asset.dto;

// This is a simplified example. You would include all fields you want to expose.
public record AssetDto(
        Long id,
        String assetTag,
        String name,
        String qrCodeId,
        String status,
        Long organizationId,
        Long assignedToUserId,
        int version
) {}
