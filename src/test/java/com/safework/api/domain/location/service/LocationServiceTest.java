package com.safework.api.domain.location.service;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.location.dto.*;
import com.safework.api.domain.location.mapper.LocationMapper;
import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.location.model.LocationType;
import com.safework.api.domain.location.repository.LocationRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    private User currentUser;
    private Organization organization;
    private Location testLocation;
    private LocationDto locationDto;
    private LocationSummaryDto locationSummaryDto;

    @BeforeEach
    void setUp() {
        // Create test organization
        organization = new Organization();
        organization.setName("Test Organization");
        setEntityId(organization, 1L);

        // Create test user
        currentUser = new User();
        currentUser.setName("Test User");
        currentUser.setEmail("test@testorg.com");
        currentUser.setRole(UserRole.ADMIN);
        currentUser.setOrganization(organization);
        setEntityId(currentUser, 1L);

        // Create test location
        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setDescription("Test location description");
        testLocation.setLocationType(LocationType.WAREHOUSE);
        testLocation.setOrganization(organization);
        testLocation.setAddress("123 Test St");
        testLocation.setCity("Test City");
        testLocation.setCountry("Test Country");
        testLocation.setMaxAssetCapacity(50);
        testLocation.setCurrentAssetCount(10);
        testLocation.setActive(true);
        setEntityId(testLocation, 1L);
        setTimestamp(testLocation, "createdAt", LocalDateTime.now());
        setTimestamp(testLocation, "updatedAt", LocalDateTime.now());

        // Create test DTOs
        locationDto = new LocationDto(
                1L, "Test Location", "Test location description", "WAREHOUSE",
                "123 Test St", "Test City", "Test Country", null, null, null,
                null, null, 50, 10, true, 1L, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        locationSummaryDto = new LocationSummaryDto(
                1L, "Test Location", "WAREHOUSE", null, null, null,
                10, 50, true
        );
    }

    private void setEntityId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID", e);
        }
    }

    private void setTimestamp(Object entity, String fieldName, LocalDateTime timestamp) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, timestamp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set timestamp", e);
        }
    }

    @Test
    void createLocation_ShouldCreateLocation_WhenValidRequest() {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "New Location", "New location description", "WAREHOUSE",
                "456 New St", "New City", "New Country", "Building A",
                "1", "Zone A", new BigDecimal("40.7128"), new BigDecimal("-74.0060"),
                100, null
        );

        Location newLocation = new Location();
        newLocation.setName("New Location");
        newLocation.setOrganization(organization);
        setEntityId(newLocation, 2L);

        when(locationRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenReturn(newLocation);
        when(locationMapper.toDto(any(Location.class))).thenReturn(locationDto);

        // When
        LocationDto result = locationService.createLocation(request, currentUser);

        // Then
        assertThat(result).isEqualTo(locationDto);
        verify(locationRepository).existsByOrganizationIdAndName(organization.getId(), request.name());
        verify(locationRepository).save(any(Location.class));
        verify(locationMapper).toDto(any(Location.class));
    }

    @Test
    void createLocation_ShouldThrowConflictException_WhenLocationNameExists() {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "Existing Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, null
        );

        when(locationRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> locationService.createLocation(request, currentUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Location with name 'Existing Location' already exists");

        verify(locationRepository).existsByOrganizationIdAndName(organization.getId(), request.name());
        verify(locationRepository, never()).save(any());
    }

    @Test
    void createLocation_ShouldThrowIllegalArgumentException_WhenInvalidLocationType() {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "New Location", null, "INVALID_TYPE", null, null, null, null,
                null, null, null, null, null, null
        );

        when(locationRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> locationService.createLocation(request, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid location type: INVALID_TYPE");

        verify(locationRepository, never()).save(any());
    }

    @Test
    void createLocation_ShouldSetParentLocation_WhenParentLocationIdProvided() {
        // Given
        Location parentLocation = new Location();
        parentLocation.setOrganization(organization);
        setEntityId(parentLocation, 3L);

        CreateLocationRequest request = new CreateLocationRequest(
                "Child Location", null, "STORAGE", null, null, null, null,
                null, null, null, null, null, 3L
        );

        when(locationRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(false);
        when(locationRepository.findById(3L)).thenReturn(Optional.of(parentLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);
        when(locationMapper.toDto(any(Location.class))).thenReturn(locationDto);

        // When
        LocationDto result = locationService.createLocation(request, currentUser);

        // Then
        assertThat(result).isNotNull();
        verify(locationRepository).findById(3L);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void findAllByOrganization_ShouldReturnPaginatedLocations() {
        // Given
        List<Location> locations = Arrays.asList(testLocation);
        Page<Location> locationPage = new PageImpl<>(locations);
        Pageable pageable = PageRequest.of(0, 10);

        when(locationRepository.findAllByOrganizationId(anyLong(), any(Pageable.class))).thenReturn(locationPage);
        when(locationMapper.toSummaryDto(any(Location.class))).thenReturn(locationSummaryDto);

        // When
        Page<LocationSummaryDto> result = locationService.findAllByOrganization(organization.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(locationSummaryDto);
        verify(locationRepository).findAllByOrganizationId(organization.getId(), pageable);
        verify(locationMapper).toSummaryDto(testLocation);
    }

    @Test
    void findActiveByOrganization_ShouldReturnOnlyActiveLocations() {
        // Given
        List<Location> activeLocations = Arrays.asList(testLocation);
        Page<Location> locationPage = new PageImpl<>(activeLocations);
        Pageable pageable = PageRequest.of(0, 10);

        when(locationRepository.findAllByOrganizationIdAndActiveTrue(anyLong(), any(Pageable.class)))
                .thenReturn(locationPage);
        when(locationMapper.toSummaryDto(any(Location.class))).thenReturn(locationSummaryDto);

        // When
        Page<LocationSummaryDto> result = locationService.findActiveByOrganization(organization.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(locationRepository).findAllByOrganizationIdAndActiveTrue(organization.getId(), pageable);
    }

    @Test
    void findLocationById_ShouldReturnLocation_WhenLocationExists() {
        // Given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(locationMapper.toDto(any(Location.class))).thenReturn(locationDto);

        // When
        LocationDto result = locationService.findLocationById(1L, currentUser);

        // Then
        assertThat(result).isEqualTo(locationDto);
        verify(locationRepository).findById(1L);
        verify(locationMapper).toDto(testLocation);
    }

    @Test
    void findLocationById_ShouldThrowResourceNotFoundException_WhenLocationNotFound() {
        // Given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> locationService.findLocationById(999L, currentUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Location not found with id: 999");

        verify(locationRepository).findById(999L);
    }

    @Test
    void findLocationById_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // Given
        Organization otherOrganization = new Organization();
        setEntityId(otherOrganization, 2L);
        
        Location otherLocation = new Location();
        otherLocation.setOrganization(otherOrganization);
        
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(otherLocation));

        // When/Then
        assertThatThrownBy(() -> locationService.findLocationById(1L, currentUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this location");
    }

    @Test
    void findLocationWithAssets_ShouldReturnLocationWithAssets() {
        // Given
        Asset asset = new Asset();
        List<Asset> assets = Arrays.asList(asset);
        
        LocationWithAssetsDto withAssetsDto = new LocationWithAssetsDto(
                1L, "Test Location", "WAREHOUSE", null, 0, 0, true, Collections.emptyList()
        );

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(assetRepository.findByLocationId(anyLong())).thenReturn(assets);
        when(locationMapper.toWithAssetsDto(any(Location.class))).thenReturn(withAssetsDto);

        // When
        LocationWithAssetsDto result = locationService.findLocationWithAssets(1L, currentUser);

        // Then
        assertThat(result).isEqualTo(withAssetsDto);
        verify(locationRepository).findById(1L);
        verify(locationMapper).toWithAssetsDto(testLocation);
    }

    @Test
    void updateLocation_ShouldUpdateLocation_WhenValidRequest() {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Updated Location", "Updated description", "OFFICE",
                "789 Updated St", "Updated City", "Updated Country", "Building B",
                "2", "Zone B", new BigDecimal("41.8781"), new BigDecimal("-87.6298"),
                75, true, null
        );

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(locationRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);
        when(locationMapper.toDto(any(Location.class))).thenReturn(locationDto);

        // When
        LocationDto result = locationService.updateLocation(1L, request, currentUser);

        // Then
        assertThat(result).isEqualTo(locationDto);
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(testLocation);
        verify(locationMapper).toDto(testLocation);
    }

    @Test
    void updateLocation_ShouldThrowConflictException_WhenNewNameAlreadyExists() {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Existing Name", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, true, null
        );

        testLocation.setName("Original Name");
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(locationRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> locationService.updateLocation(1L, request, currentUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Location with name 'Existing Name' already exists");

        verify(locationRepository, never()).save(any());
    }

    @Test
    void updateLocation_ShouldThrowIllegalArgumentException_WhenLocationIsItsOwnParent() {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Test Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, true, 1L // Same as location ID
        );

        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        // When/Then
        assertThatThrownBy(() -> locationService.updateLocation(1L, request, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location cannot be its own parent");
    }

    @Test
    void deleteLocation_ShouldDeleteLocation_WhenNoAssetsOrChildren() {
        // Given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(locationRepository.countAssetsByLocationId(anyLong())).thenReturn(0L);
        when(locationRepository.findByParentLocationId(anyLong())).thenReturn(Collections.emptyList());

        // When
        locationService.deleteLocation(1L, currentUser);

        // Then
        verify(locationRepository).findById(1L);
        verify(locationRepository).countAssetsByLocationId(1L);
        verify(locationRepository).findByParentLocationId(1L);
        verify(locationRepository).delete(testLocation);
    }

    @Test
    void deleteLocation_ShouldThrowConflictException_WhenLocationHasAssets() {
        // Given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(locationRepository.countAssetsByLocationId(anyLong())).thenReturn(5L);

        // When/Then
        assertThatThrownBy(() -> locationService.deleteLocation(1L, currentUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot delete location with 5 asset(s)");

        verify(locationRepository, never()).delete(any());
    }

    @Test
    void deleteLocation_ShouldThrowConflictException_WhenLocationHasChildren() {
        // Given
        Location childLocation = new Location();
        List<Location> children = Arrays.asList(childLocation);

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(locationRepository.countAssetsByLocationId(anyLong())).thenReturn(0L);
        when(locationRepository.findByParentLocationId(anyLong())).thenReturn(children);

        // When/Then
        assertThatThrownBy(() -> locationService.deleteLocation(1L, currentUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot delete location with child locations");

        verify(locationRepository, never()).delete(any());
    }

    @Test
    void updateAssetCount_ShouldUpdateCount_WhenLocationExists() {
        // Given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(locationRepository.countAssetsByLocationId(anyLong())).thenReturn(15L);
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        // When
        locationService.updateAssetCount(1L);

        // Then
        verify(locationRepository).findById(1L);
        verify(locationRepository).countAssetsByLocationId(1L);
        verify(locationRepository).save(testLocation);
    }

    @Test
    void updateAssetCount_ShouldThrowResourceNotFoundException_WhenLocationNotFound() {
        // Given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> locationService.updateAssetCount(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Location not found with id: 999");
    }

    @Test
    void transferAssetToLocation_ShouldTransferAsset_WhenValidRequest() {
        // Given
        Asset asset = new Asset();
        asset.setOrganization(organization);
        setEntityId(asset, 1L);

        testLocation.setMaxAssetCapacity(50);
        testLocation.setCurrentAssetCount(10);

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(assetRepository.findById(anyLong())).thenReturn(Optional.of(asset));
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        when(locationRepository.countAssetsByLocationId(anyLong())).thenReturn(11L);
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        // When
        locationService.transferAssetToLocation(1L, 1L, currentUser);

        // Then
        verify(locationRepository, times(2)).findById(1L); // Called by both getLocationForUser and updateAssetCount
        verify(assetRepository).findById(1L);
        verify(assetRepository).save(asset);
    }

    @Test
    void transferAssetToLocation_ShouldThrowConflictException_WhenLocationAtCapacity() {
        // Given
        Asset asset = new Asset();
        asset.setOrganization(organization);

        testLocation.setMaxAssetCapacity(10);
        testLocation.setCurrentAssetCount(10); // At capacity

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(assetRepository.findById(anyLong())).thenReturn(Optional.of(asset));

        // When/Then
        assertThatThrownBy(() -> locationService.transferAssetToLocation(1L, 1L, currentUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Location 'Test Location' is at capacity");

        verify(assetRepository, never()).save(any());
    }

    @Test
    void transferAssetToLocation_ShouldThrowAccessDeniedException_WhenAssetFromDifferentOrganization() {
        // Given
        Organization otherOrganization = new Organization();
        setEntityId(otherOrganization, 2L);

        Asset asset = new Asset();
        asset.setOrganization(otherOrganization);

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(testLocation));
        when(assetRepository.findById(anyLong())).thenReturn(Optional.of(asset));

        // When/Then
        assertThatThrownBy(() -> locationService.transferAssetToLocation(1L, 1L, currentUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this asset");
    }

    @Test
    void findAvailableLocations_ShouldReturnAvailableLocations() {
        // Given
        List<Location> availableLocations = Arrays.asList(testLocation);
        Page<Location> locationPage = new PageImpl<>(availableLocations);
        Pageable pageable = PageRequest.of(0, 10);

        when(locationRepository.findAvailableLocationsByOrganizationId(anyLong(), any(Pageable.class)))
                .thenReturn(locationPage);
        when(locationMapper.toSummaryDto(any(Location.class))).thenReturn(locationSummaryDto);

        // When
        Page<LocationSummaryDto> result = locationService.findAvailableLocations(organization.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(locationSummaryDto);
        verify(locationRepository).findAvailableLocationsByOrganizationId(organization.getId(), pageable);
    }

    @Test
    void createLocation_ShouldSetDefaultValues_WhenMinimalRequest() {
        // Given
        CreateLocationRequest minimalRequest = new CreateLocationRequest(
                "Minimal Location", null, "OTHER", null, null, null, null,
                null, null, null, null, null, null
        );

        Location savedLocation = new Location();
        savedLocation.setName("Minimal Location");
        savedLocation.setOrganization(organization);
        savedLocation.setActive(true);
        savedLocation.setCurrentAssetCount(0);

        when(locationRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);
        when(locationMapper.toDto(any(Location.class))).thenReturn(locationDto);

        // When
        LocationDto result = locationService.createLocation(minimalRequest, currentUser);

        // Then
        assertThat(result).isNotNull();
        verify(locationRepository).save(argThat(location -> 
                location.getActive().equals(true) && 
                location.getCurrentAssetCount().equals(0)));
    }
}