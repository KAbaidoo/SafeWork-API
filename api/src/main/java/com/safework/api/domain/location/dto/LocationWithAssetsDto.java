package com.safework.api.domain.location.dto;

import java.util.List;

public record LocationWithAssetsDto(
        Long id,
        String name,
        String locationType,
        String description,
        Integer currentAssetCount,
        Integer maxAssetCapacity,
        Boolean active,
        List<AssetSummaryDto> assets
) {
    public record AssetSummaryDto(
            Long id,
            String assetTag,
            String name,
            String status
    ) {}
}