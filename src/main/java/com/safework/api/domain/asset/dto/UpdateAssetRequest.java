package com.safework.api.domain.asset.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAssetRequest(
        @NotBlank(message = "Asset name cannot be blank")
        String name,

        // assignedToUserId can be null if un-assigning an asset
        Long assignedToUserId,

        // A custom validator could be used here to ensure the string matches a valid AssetStatus enum value
        @NotBlank(message = "Status is required")
        String status,

        @NotNull(message = "Version is required for updates")
        @Min(value = 0, message = "Version cannot be negative")
        int version
) {}