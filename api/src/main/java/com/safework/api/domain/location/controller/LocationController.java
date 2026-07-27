package com.safework.api.domain.location.controller;

import com.safework.api.domain.location.dto.*;
import com.safework.api.domain.location.service.LocationService;
import com.safework.api.domain.user.model.User;
import com.safework.api.security.PrincipalUser;
import jakarta.validation.Valid;
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
@RequestMapping("/v1/locations")
public class LocationController {

    private final LocationService locationService;

    /**
     * Creates a new location. Requires ADMIN or SUPERVISOR role.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERVISOR')")
    public ResponseEntity<LocationDto> createLocation(
            @Valid @RequestBody CreateLocationRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        LocationDto newLocation = locationService.createLocation(request, currentUser);
        return new ResponseEntity<>(newLocation, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all locations for the current user's organization.
     * All authenticated users can view locations.
     */
    @GetMapping
    public ResponseEntity<Page<LocationSummaryDto>> getLocationsByOrganization(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        
        Page<LocationSummaryDto> locations = activeOnly ? 
                locationService.findActiveByOrganization(currentUser.getOrganization().getId(), pageable) :
                locationService.findAllByOrganization(currentUser.getOrganization().getId(), pageable);
        
        return ResponseEntity.ok(locations);
    }

    /**
     * Retrieves a single location by its unique ID.
     * All authenticated users can view location details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        LocationDto location = locationService.findLocationById(id, currentUser);
        return ResponseEntity.ok(location);
    }

    /**
     * Retrieves a location with its associated assets.
     * All authenticated users can view location and asset details.
     */
    @GetMapping("/{id}/assets")
    public ResponseEntity<LocationWithAssetsDto> getLocationWithAssets(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        LocationWithAssetsDto location = locationService.findLocationWithAssets(id, currentUser);
        return ResponseEntity.ok(location);
    }

    /**
     * Updates an existing location. Requires ADMIN or SUPERVISOR role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERVISOR')")
    public ResponseEntity<LocationDto> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        LocationDto updatedLocation = locationService.updateLocation(id, request, currentUser);
        return ResponseEntity.ok(updatedLocation);
    }

    /**
     * Deletes a location. Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        locationService.deleteLocation(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Transfers an asset to a location. Requires ADMIN or SUPERVISOR role.
     */
    @PostMapping("/{id}/transfer-asset/{assetId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERVISOR')")
    public ResponseEntity<Void> transferAssetToLocation(
            @PathVariable Long id,
            @PathVariable Long assetId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        locationService.transferAssetToLocation(assetId, id, currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves locations with available capacity for the current user's organization.
     * Useful for asset placement workflows.
     */
    @GetMapping("/available")
    public ResponseEntity<Page<LocationSummaryDto>> getAvailableLocations(
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<LocationSummaryDto> locations = locationService.findAvailableLocations(
                currentUser.getOrganization().getId(), pageable);
        return ResponseEntity.ok(locations);
    }
}