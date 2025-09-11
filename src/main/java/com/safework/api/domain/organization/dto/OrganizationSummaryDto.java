package com.safework.api.domain.organization.dto;

public record OrganizationSummaryDto(
        Long id,
        String name,
        String industry,
        String size
) {}