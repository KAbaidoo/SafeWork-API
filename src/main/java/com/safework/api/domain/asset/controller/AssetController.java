package com.safework.api.domain.asset.controller;

import com.safework.api.domain.asset.dto.AssetDto;
import com.safework.api.domain.asset.dto.CreateAssetRequest;
import com.safework.api.domain.asset.dto.UpdateAssetRequest;
import com.safework.api.domain.asset.service.AssetService;
import com.safework.api.domain.user.model.User;
import com.safework.api.security.PrincipalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/assets") // All endpoints are versioned
public class AssetController {

    private final AssetService assetService;

    /**
     * Creates a new asset. Requires ADMIN role.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AssetDto> createAsset(@RequestBody CreateAssetRequest request, @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        AssetDto newAsset = assetService.createAsset(request, currentUser);
        return new ResponseEntity<>(newAsset, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all assets for the current user's organization.
     */
    @GetMapping
    public ResponseEntity<Page<AssetDto>> getAssetsByOrganization(@AuthenticationPrincipal PrincipalUser principalUser, Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<AssetDto> assets = assetService.findAllByOrganization(currentUser.getOrganization().getId(), pageable);
        return ResponseEntity.ok(assets);
    }

    /**
     * Retrieves a single asset by its unique ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssetDto> getAssetById(@PathVariable Long id, @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        AssetDto asset = assetService.findAssetById(id, currentUser);
        return ResponseEntity.ok(asset);
    }

    /**
     * Updates an existing asset. Requires ADMIN role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AssetDto> updateAsset(@PathVariable Long id, @RequestBody UpdateAssetRequest request, @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        AssetDto updatedAsset = assetService.updateAsset(id, request, currentUser);
        return ResponseEntity.ok(updatedAsset);
    }

    /**
     * Retrieves a single asset by its QR code ID.
     */
    @GetMapping("/qr/{qrCodeId}")
    public ResponseEntity<AssetDto> getAssetByQrCode(@PathVariable String qrCodeId, @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        AssetDto asset = assetService.findAssetByQrCode(qrCodeId, currentUser);
        return ResponseEntity.ok(asset);
    }

    /**
     * Deletes an asset. Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id, @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        assetService.deleteAsset(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}