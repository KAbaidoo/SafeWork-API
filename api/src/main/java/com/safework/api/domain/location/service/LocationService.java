package com.safework.api.domain.location.service;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.location.dto.*;
import com.safework.api.domain.location.mapper.LocationMapper;
import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.location.model.LocationType;
import com.safework.api.domain.location.repository.LocationRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final AssetRepository assetRepository;
    private final LocationMapper locationMapper;

    public LocationDto createLocation(CreateLocationRequest request, User currentUser) {
        // Check if location name already exists within the organization
        if (locationRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Location with name '" + request.name() + "' already exists in this organization");
        }

        Location newLocation = new Location();
        newLocation.setOrganization(currentUser.getOrganization());
        newLocation.setName(request.name());
        newLocation.setDescription(request.description());
        
        // Set location type
        try {
            newLocation.setLocationType(LocationType.valueOf(request.locationType()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid location type: " + request.locationType());
        }

        newLocation.setAddress(request.address());
        newLocation.setCity(request.city());
        newLocation.setCountry(request.country());
        newLocation.setBuildingName(request.buildingName());
        newLocation.setFloor(request.floor());
        newLocation.setZone(request.zone());
        newLocation.setLatitude(request.latitude());
        newLocation.setLongitude(request.longitude());
        newLocation.setMaxAssetCapacity(request.maxAssetCapacity());
        newLocation.setCurrentAssetCount(0); // Initialize with 0
        newLocation.setActive(true); // New locations are active by default

        // Set parent location if provided
        if (request.parentLocationId() != null) {
            Location parentLocation = getLocationForUser(request.parentLocationId(), currentUser);
            newLocation.setParentLocation(parentLocation);
        }

        Location savedLocation = locationRepository.save(newLocation);
        return locationMapper.toDto(savedLocation);
    }

    @Transactional(readOnly = true)
    public Page<LocationSummaryDto> findAllByOrganization(Long organizationId, Pageable pageable) {
        Page<Location> locations = locationRepository.findAllByOrganizationId(organizationId, pageable);
        return locations.map(locationMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<LocationSummaryDto> findActiveByOrganization(Long organizationId, Pageable pageable) {
        Page<Location> locations = locationRepository.findAllByOrganizationIdAndActiveTrue(organizationId, pageable);
        return locations.map(locationMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public LocationDto findLocationById(Long id, User currentUser) {
        Location location = getLocationForUser(id, currentUser);
        return locationMapper.toDto(location);
    }

    @Transactional(readOnly = true)
    public LocationWithAssetsDto findLocationWithAssets(Long id, User currentUser) {
        Location location = getLocationForUser(id, currentUser);
        // Ensure assets are loaded
        if (location.getAssets() == null) {
            location.setAssets(assetRepository.findByLocationId(id));
        }
        return locationMapper.toWithAssetsDto(location);
    }

    public LocationDto updateLocation(Long id, UpdateLocationRequest request, User currentUser) {
        Location locationToUpdate = getLocationForUser(id, currentUser);

        // Check if name is being changed and if it already exists
        if (!locationToUpdate.getName().equals(request.name()) && 
            locationRepository.existsByOrganizationIdAndName(currentUser.getOrganization().getId(), request.name())) {
            throw new ConflictException("Location with name '" + request.name() + "' already exists in this organization");
        }

        locationToUpdate.setName(request.name());
        locationToUpdate.setDescription(request.description());
        
        // Update location type
        try {
            locationToUpdate.setLocationType(LocationType.valueOf(request.locationType()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid location type: " + request.locationType());
        }

        locationToUpdate.setAddress(request.address());
        locationToUpdate.setCity(request.city());
        locationToUpdate.setCountry(request.country());
        locationToUpdate.setBuildingName(request.buildingName());
        locationToUpdate.setFloor(request.floor());
        locationToUpdate.setZone(request.zone());
        locationToUpdate.setLatitude(request.latitude());
        locationToUpdate.setLongitude(request.longitude());
        locationToUpdate.setMaxAssetCapacity(request.maxAssetCapacity());
        locationToUpdate.setActive(request.active());

        // Update parent location if provided
        if (request.parentLocationId() != null) {
            Location parentLocation = getLocationForUser(request.parentLocationId(), currentUser);
            // Prevent circular hierarchy
            if (parentLocation.getId().equals(locationToUpdate.getId())) {
                throw new IllegalArgumentException("Location cannot be its own parent");
            }
            locationToUpdate.setParentLocation(parentLocation);
        } else {
            locationToUpdate.setParentLocation(null);
        }

        Location savedLocation = locationRepository.save(locationToUpdate);
        return locationMapper.toDto(savedLocation);
    }

    public void deleteLocation(Long id, User currentUser) {
        Location locationToDelete = getLocationForUser(id, currentUser);
        
        // Check if location has assets
        long assetCount = locationRepository.countAssetsByLocationId(locationToDelete.getId());
        if (assetCount > 0) {
            throw new ConflictException("Cannot delete location with " + assetCount + " asset(s). Please relocate assets first.");
        }

        // Check if location has child locations
        if (!locationRepository.findByParentLocationId(locationToDelete.getId()).isEmpty()) {
            throw new ConflictException("Cannot delete location with child locations. Please reassign child locations first.");
        }

        locationRepository.delete(locationToDelete);
    }

    /**
     * Updates the asset count for a location.
     * This method should be called when assets are assigned/unassigned to locations.
     */
    public void updateAssetCount(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));
        
        long assetCount = locationRepository.countAssetsByLocationId(locationId);
        location.setCurrentAssetCount((int) assetCount);
        locationRepository.save(location);
    }

    /**
     * Transfers an asset to a new location with capacity validation.
     */
    public void transferAssetToLocation(Long assetId, Long locationId, User currentUser) {
        Location targetLocation = getLocationForUser(locationId, currentUser);
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        // Multi-tenancy check for asset
        if (!asset.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("You do not have permission to access this asset");
        }

        // Check capacity if location has a limit
        if (targetLocation.getMaxAssetCapacity() != null) {
            int currentCount = targetLocation.getCurrentAssetCount() != null ? targetLocation.getCurrentAssetCount() : 0;
            if (currentCount >= targetLocation.getMaxAssetCapacity()) {
                throw new ConflictException("Location '" + targetLocation.getName() + "' is at capacity (" + 
                                           currentCount + "/" + targetLocation.getMaxAssetCapacity() + ")");
            }
        }

        Location previousLocation = asset.getLocation();
        asset.setLocation(targetLocation);
        assetRepository.save(asset);

        // Update asset counts
        updateAssetCount(locationId);
        if (previousLocation != null) {
            updateAssetCount(previousLocation.getId());
        }
    }

    @Transactional(readOnly = true)
    public Page<LocationSummaryDto> findAvailableLocations(Long organizationId, Pageable pageable) {
        Page<Location> locations = locationRepository.findAvailableLocationsByOrganizationId(organizationId, pageable);
        return locations.map(locationMapper::toSummaryDto);
    }

    /**
     * Helper method to fetch a location and verify the user has permission to access it.
     */
    private Location getLocationForUser(Long locationId, User currentUser) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Multi-tenancy security check - users can only access locations in their organization
        if (!location.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("You do not have permission to access this location");
        }

        return location;
    }
}