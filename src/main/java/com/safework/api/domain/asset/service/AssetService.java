package com.safework.api.domain.asset.service;

import com.safework.api.domain.asset.dto.AssetDto;
import com.safework.api.domain.asset.dto.CreateAssetRequest;
import com.safework.api.domain.asset.dto.UpdateAssetRequest;
import com.safework.api.domain.asset.mapper.AssetMapper;
import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.asset.repository.AssetTypeRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // Ensures all database operations in a method are atomic
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final AssetMapper assetMapper;

    public AssetService(AssetRepository assetRepository, AssetTypeRepository assetTypeRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetTypeRepository = assetTypeRepository;
        this.assetMapper = assetMapper;
    }

    public AssetDto createAsset(CreateAssetRequest request, User currentUser) {
        var assetType = assetTypeRepository.findById(request.assetTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("AssetType not found with id: " + request.assetTypeId()));

        Asset newAsset = new Asset();
        newAsset.setOrganization(currentUser.getOrganization());
        newAsset.setAssetType(assetType);
        newAsset.setAssetTag(request.assetTag());
        newAsset.setName(request.name());
        newAsset.setQrCodeId(request.qrCodeId());
        newAsset.setStatus(AssetStatus.INACTIVE); // Default status on creation

        Asset savedAsset = assetRepository.save(newAsset);
        return assetMapper.toDto(savedAsset);
    }

    @Transactional(readOnly = true)
    public Page<AssetDto> findAllByOrganization(Long organizationId, Pageable pageable) {
        Page<Asset> assets = assetRepository.findAllByOrganizationId(organizationId, pageable);
        return assets.map(assetMapper::toDto);
    }

    @Transactional(readOnly = true)
    public AssetDto findAssetById(Long id, User currentUser) {
        Asset asset = getAssetForUser(id, currentUser);
        return assetMapper.toDto(asset);
    }

    public AssetDto updateAsset(Long id, UpdateAssetRequest request, User currentUser) {
        Asset assetToUpdate = getAssetForUser(id, currentUser);

        // --- Optimistic Locking for Offline Sync ---
        // This check is critical. If the version from the client does not match the
        // database version, it means the data is stale, and we must reject the update.
        if (request.version() != assetToUpdate.getVersion()) {
            throw new ConflictException("Conflict: Asset has been updated by another user. Please refresh and try again.");
        }

        assetToUpdate.setName(request.name());
        assetToUpdate.setStatus(AssetStatus.valueOf(request.status()));
        // You would also update assignedTo user here by fetching from the UserRepository

        Asset savedAsset = assetRepository.save(assetToUpdate);
        return assetMapper.toDto(savedAsset);
    }

    public void deleteAsset(Long id, User currentUser) {
        Asset assetToDelete = getAssetForUser(id, currentUser);
        assetRepository.delete(assetToDelete);
    }

    /**
     * Helper method to fetch an asset and verify the user has permission to access it.
     */
    private Asset getAssetForUser(Long assetId, User user) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        // --- Multi-Tenancy Security Check ---
        if (!asset.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new AccessDeniedException("You do not have permission to access this asset.");
        }
        return asset;
    }
}