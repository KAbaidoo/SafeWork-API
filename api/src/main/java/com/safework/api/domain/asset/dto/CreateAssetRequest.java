package com.safework.api.domain.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAssetRequest(
        @NotBlank(message = "Asset tag is required")
        @Size(min = 3, max = 50, message = "Asset tag must be between 3 and 50 characters")
        String assetTag,

        @NotBlank(message = "Asset name is required")
        String name,

        @NotBlank(message = "QR code ID is required")
        String qrCodeId,

        @NotNull(message = "Asset type ID is required")
        Long assetTypeId
) {}