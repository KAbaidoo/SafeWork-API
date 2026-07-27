package com.safework.api.domain.location.mapper;

import com.safework.api.domain.location.dto.LocationDto;
import com.safework.api.domain.location.dto.LocationSummaryDto;
import com.safework.api.domain.location.dto.LocationWithAssetsDto;
import com.safework.api.domain.location.model.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationDto toDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getDescription(),
                location.getLocationType() != null ? location.getLocationType().name() : null,
                location.getAddress(),
                location.getCity(),
                location.getCountry(),
                location.getBuildingName(),
                location.getFloor(),
                location.getZone(),
                location.getLatitude(),
                location.getLongitude(),
                location.getMaxAssetCapacity(),
                location.getCurrentAssetCount(),
                location.getActive(),
                location.getOrganization() != null ? location.getOrganization().getId() : null,
                location.getParentLocation() != null ? location.getParentLocation().getId() : null,
                location.getParentLocation() != null ? location.getParentLocation().getName() : null,
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }

    public LocationSummaryDto toSummaryDto(Location location) {
        return new LocationSummaryDto(
                location.getId(),
                location.getName(),
                location.getLocationType() != null ? location.getLocationType().name() : null,
                location.getBuildingName(),
                location.getFloor(),
                location.getZone(),
                location.getCurrentAssetCount(),
                location.getMaxAssetCapacity(),
                location.getActive()
        );
    }

    public LocationWithAssetsDto toWithAssetsDto(Location location) {
        var assetSummaries = location.getAssets() != null ? 
                location.getAssets().stream()
                    .map(asset -> new LocationWithAssetsDto.AssetSummaryDto(
                            asset.getId(),
                            asset.getAssetTag(),
                            asset.getName(),
                            asset.getStatus() != null ? asset.getStatus().name() : null
                    ))
                    .toList() : null;

        return new LocationWithAssetsDto(
                location.getId(),
                location.getName(),
                location.getLocationType() != null ? location.getLocationType().name() : null,
                location.getDescription(),
                location.getCurrentAssetCount(),
                location.getMaxAssetCapacity(),
                location.getActive(),
                assetSummaries
        );
    }
}