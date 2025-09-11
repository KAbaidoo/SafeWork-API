package com.safework.api.domain.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Pattern(regexp = "^[+]?[\\d\\s\\-()]+$", message = "Phone number format is invalid")
        String phone,

        @Pattern(regexp = "^(https?://)?[\\w.-]+\\.[a-zA-Z]{2,}$", message = "Website URL format is invalid")
        String website,

        @Size(max = 100, message = "Industry must not exceed 100 characters")
        String industry,

        @Pattern(regexp = "^(SMALL|MEDIUM|LARGE|ENTERPRISE)$", message = "Size must be one of: SMALL, MEDIUM, LARGE, ENTERPRISE")
        String size
) {}