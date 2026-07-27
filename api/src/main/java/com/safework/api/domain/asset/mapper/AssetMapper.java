package com.safework.api.domain.asset.mapper;

import com.safework.api.domain.asset.dto.AssetDto;
import com.safework.api.domain.asset.model.Asset;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

    public AssetDto toDto(Asset asset) {
        return new AssetDto(
                asset.getId(),
                asset.getAssetTag(),
                asset.getName(),
                asset.getQrCodeId(),
                asset.getStatus().name(),
                asset.getOrganization().getId(),
                asset.getAssignedTo() != null ? asset.getAssignedTo().getId() : null,
                asset.getVersion()
        );
    }
}