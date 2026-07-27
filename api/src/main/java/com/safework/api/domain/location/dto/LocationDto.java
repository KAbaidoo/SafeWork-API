package com.safework.api.domain.location.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LocationDto(
        Long id,
        String name,
        String description,
        String locationType,
        String address,
        String city,
        String country,
        String buildingName,
        String floor,
        String zone,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer maxAssetCapacity,
        Integer currentAssetCount,
        Boolean active,
        Long organizationId,
        Long parentLocationId,
        String parentLocationName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}