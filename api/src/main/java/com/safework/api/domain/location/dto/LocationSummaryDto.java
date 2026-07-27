package com.safework.api.domain.location.dto;

public record LocationSummaryDto(
        Long id,
        String name,
        String locationType,
        String buildingName,
        String floor,
        String zone,
        Integer currentAssetCount,
        Integer maxAssetCapacity,
        Boolean active
) {}