package com.safework.api.domain.location.integration;

import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.location.dto.*;
import com.safework.api.domain.location.mapper.LocationMapper;
import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.location.model.LocationType;
import com.safework.api.domain.location.repository.LocationRepository;
import com.safework.api.domain.location.service.LocationService;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class LocationIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AssetRepository assetRepository;

    private LocationService locationService;
    
    private Organization testOrganization;
    private Organization otherOrganization;
    private User adminUser;
    private User supervisorUser;
    private User inspectorUser;
    private User otherOrgUser;
    private Location testLocation;
    private Location parentLocation;

    @BeforeEach
    void setUp() {
        // Create location service with real dependencies
        locationService = new LocationService(
                locationRepository,
                assetRepository,
                new LocationMapper()
        );

        // Create test organization
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        entityManager.persistAndFlush(testOrganization);

        // Create other organization for multi-tenant testing
        otherOrganization = new Organization();
        otherOrganization.setName("Other Organization");
        entityManager.persistAndFlush(otherOrganization);

        // Create test users
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@testorg.com");
        adminUser.setPassword("hashedPassword");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(testOrganization);
        entityManager.persistAndFlush(adminUser);

        supervisorUser = new User();
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@testorg.com");
        supervisorUser.setPassword("hashedPassword");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(testOrganization);
        entityManager.persistAndFlush(supervisorUser);

        inspectorUser = new User();
        inspectorUser.setName("Inspector User");
        inspectorUser.setEmail("inspector@testorg.com");
        inspectorUser.setPassword("hashedPassword");
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(testOrganization);
        entityManager.persistAndFlush(inspectorUser);

        // Create user from other organization
        otherOrgUser = new User();
        otherOrgUser.setName("Other Org User");
        otherOrgUser.setEmail("user@otherorg.com");
        otherOrgUser.setPassword("hashedPassword");
        otherOrgUser.setRole(UserRole.ADMIN);
        otherOrgUser.setOrganization(otherOrganization);
        entityManager.persistAndFlush(otherOrgUser);

        // Create parent location
        parentLocation = new Location();
        parentLocation.setName("Parent Location");
        parentLocation.setLocationType(LocationType.WAREHOUSE);
        parentLocation.setOrganization(testOrganization);
        parentLocation.setMaxAssetCapacity(200);
        parentLocation.setCurrentAssetCount(50);
        parentLocation.setActive(true);
        entityManager.persistAndFlush(parentLocation);

        // Create test location
        testLocation = new Location();
        testLocation.setName("Integration Test Location");
        testLocation.setDescription("Location for integration testing");
        testLocation.setLocationType(LocationType.WAREHOUSE);
        testLocation.setOrganization(testOrganization);
        testLocation.setAddress("123 Integration St");
        testLocation.setCity("Test City");
        testLocation.setCountry("Test Country");
        testLocation.setBuildingName("Test Building");
        testLocation.setFloor("1");
        testLocation.setZone("Zone A");
        testLocation.setLatitude(new BigDecimal("40.7128"));
        testLocation.setLongitude(new BigDecimal("-74.0060"));
        testLocation.setMaxAssetCapacity(100);
        testLocation.setCurrentAssetCount(25);
        testLocation.setActive(true);
        testLocation.setParentLocation(parentLocation);
        entityManager.persistAndFlush(testLocation);

        entityManager.clear();
    }

    @Test
    void createLocation_ShouldCreateLocationSuccessfully_WhenValidRequest() {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "New Integration Location",
                "New location for integration testing",
                "OFFICE",
                "456 New Integration St",
                "New Test City",
                "New Test Country",
                "New Test Building",
                "2",
                "Zone B",
                new BigDecimal("41.8781"),
                new BigDecimal("-87.6298"),
                150,
                null
        );

        // When
        LocationDto result = locationService.createLocation(request, adminUser);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("New Integration Location");
        assertThat(result.locationType()).isEqualTo("OFFICE");
        assertThat(result.address()).isEqualTo("456 New Integration St");
        assertThat(result.city()).isEqualTo("New Test City");
        assertThat(result.country()).isEqualTo("New Test Country");
        assertThat(result.buildingName()).isEqualTo("New Test Building");
        assertThat(result.floor()).isEqualTo("2");
        assertThat(result.zone()).isEqualTo("Zone B");
        assertThat(result.maxAssetCapacity()).isEqualTo(150);
        assertThat(result.currentAssetCount()).isEqualTo(0);
        assertThat(result.active()).isTrue();
        assertThat(result.organizationId()).isEqualTo(testOrganization.getId());
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();

        // Verify location was saved to database
        Location savedLocation = locationRepository.findById(result.id()).orElse(null);
        assertThat(savedLocation).isNotNull();
        assertThat(savedLocation.getName()).isEqualTo("New Integration Location");
        assertThat(savedLocation.getLocationType()).isEqualTo(LocationType.OFFICE);
        assertThat(savedLocation.getOrganization().getId()).isEqualTo(testOrganization.getId());
    }

    @Test
    void createLocation_ShouldCreateWithParentLocation_WhenParentLocationIdProvided() {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "Child Location",
                "Child of parent location",
                "STORAGE",
                null, null, null, null, "1", "Zone A-1",
                null, null, 50, parentLocation.getId()
        );

        // When
        LocationDto result = locationService.createLocation(request, adminUser);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Child Location");
        assertThat(result.locationType()).isEqualTo("STORAGE");
        assertThat(result.parentLocationId()).isEqualTo(parentLocation.getId());
        assertThat(result.parentLocationName()).isEqualTo("Parent Location");

        // Verify hierarchy was created in database
        Location savedChild = locationRepository.findById(result.id()).orElse(null);
        assertThat(savedChild).isNotNull();
        assertThat(savedChild.getParentLocation()).isNotNull();
        assertThat(savedChild.getParentLocation().getId()).isEqualTo(parentLocation.getId());
    }

    @Test
    void createLocation_ShouldThrowConflictException_WhenLocationNameAlreadyExists() {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "Integration Test Location", // Same name as existing location
                null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, null
        );

        // When/Then
        assertThatThrownBy(() -> locationService.createLocation(request, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Location with name 'Integration Test Location' already exists in this organization");
    }

    @Test
    void findAllByOrganization_ShouldReturnLocationsList() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LocationSummaryDto> result = locationService.findAllByOrganization(testOrganization.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(2); // parentLocation + testLocation
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(LocationSummaryDto::name)
                .containsExactlyInAnyOrder("Parent Location", "Integration Test Location");
    }

    @Test
    void findActiveByOrganization_ShouldReturnActiveLocationsOnly() {
        // Given - Create inactive location
        Location inactiveLocation = new Location();
        inactiveLocation.setName("Inactive Location");
        inactiveLocation.setLocationType(LocationType.OFFICE);
        inactiveLocation.setOrganization(testOrganization);
        inactiveLocation.setActive(false);
        entityManager.persistAndFlush(inactiveLocation);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LocationSummaryDto> result = locationService.findActiveByOrganization(testOrganization.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(2); // Only active locations
        assertThat(result.getContent()).extracting(LocationSummaryDto::name)
                .containsExactlyInAnyOrder("Parent Location", "Integration Test Location");
        assertThat(result.getContent()).allMatch(LocationSummaryDto::active);
    }

    @Test
    void findLocationById_ShouldReturnLocation_WhenLocationExists() {
        // When
        LocationDto result = locationService.findLocationById(testLocation.getId(), inspectorUser);

        // Then
        assertThat(result.id()).isEqualTo(testLocation.getId());
        assertThat(result.name()).isEqualTo("Integration Test Location");
        assertThat(result.locationType()).isEqualTo("WAREHOUSE");
        assertThat(result.address()).isEqualTo("123 Integration St");
        assertThat(result.maxAssetCapacity()).isEqualTo(100);
        assertThat(result.currentAssetCount()).isEqualTo(25);
        assertThat(result.parentLocationId()).isEqualTo(parentLocation.getId());
        assertThat(result.parentLocationName()).isEqualTo("Parent Location");
    }

    @Test
    void findLocationById_ShouldThrowResourceNotFoundException_WhenLocationDoesNotExist() {
        // When/Then
        assertThatThrownBy(() -> locationService.findLocationById(999999L, inspectorUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Location not found with id: 999999");
    }

    @Test
    void findLocationById_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // When/Then
        assertThatThrownBy(() -> locationService.findLocationById(testLocation.getId(), otherOrgUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this location");
    }

    @Test
    void findLocationWithAssets_ShouldReturnLocationWithEmptyAssetsList() {
        // When
        LocationWithAssetsDto result = locationService.findLocationWithAssets(testLocation.getId(), inspectorUser);

        // Then
        assertThat(result.id()).isEqualTo(testLocation.getId());
        assertThat(result.name()).isEqualTo("Integration Test Location");
        assertThat(result.locationType()).isEqualTo("WAREHOUSE");
        assertThat(result.description()).isEqualTo("Location for integration testing");
        assertThat(result.currentAssetCount()).isEqualTo(25);
        assertThat(result.maxAssetCapacity()).isEqualTo(100);
        assertThat(result.active()).isTrue();
        assertThat(result.assets()).isEmpty(); // No assets in test setup
    }

    @Test
    void updateLocation_ShouldUpdateLocationSuccessfully_WhenValidRequest() {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Updated Integration Location",
                "Updated description for integration testing",
                "OFFICE",
                "789 Updated Integration St",
                "Updated Test City",
                "Updated Test Country",
                "Updated Test Building",
                "3",
                "Zone C",
                new BigDecimal("42.3601"),
                new BigDecimal("-71.0589"),
                200,
                true,
                null
        );

        // When
        LocationDto result = locationService.updateLocation(testLocation.getId(), request, adminUser);

        // Then
        assertThat(result.id()).isEqualTo(testLocation.getId());
        assertThat(result.name()).isEqualTo("Updated Integration Location");
        assertThat(result.locationType()).isEqualTo("OFFICE");
        assertThat(result.address()).isEqualTo("789 Updated Integration St");
        assertThat(result.maxAssetCapacity()).isEqualTo(200);

        // Verify location was updated in database
        Location updatedLocation = locationRepository.findById(testLocation.getId()).orElse(null);
        assertThat(updatedLocation).isNotNull();
        assertThat(updatedLocation.getName()).isEqualTo("Updated Integration Location");
        assertThat(updatedLocation.getLocationType()).isEqualTo(LocationType.OFFICE);
        assertThat(updatedLocation.getMaxAssetCapacity()).isEqualTo(200);
    }

    @Test
    void updateLocation_ShouldThrowConflictException_WhenNewNameAlreadyExists() {
        // Given - Create another location with a different name
        Location otherLocation = new Location();
        otherLocation.setName("Other Location");
        otherLocation.setLocationType(LocationType.OFFICE);
        otherLocation.setOrganization(testOrganization);
        otherLocation.setActive(true);
        entityManager.persistAndFlush(otherLocation);

        UpdateLocationRequest request = new UpdateLocationRequest(
                "Other Location", // Try to change to existing name
                null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, true, null
        );

        // When/Then
        assertThatThrownBy(() -> locationService.updateLocation(testLocation.getId(), request, adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Location with name 'Other Location' already exists");
    }

    @Test
    void deleteLocation_ShouldDeleteLocationSuccessfully_WhenNoAssetsOrChildren() {
        // Given - Create a location without assets or children
        Location locationToDelete = new Location();
        locationToDelete.setName("Location To Delete");
        locationToDelete.setLocationType(LocationType.YARD);
        locationToDelete.setOrganization(testOrganization);
        locationToDelete.setActive(true);
        entityManager.persistAndFlush(locationToDelete);

        // When
        locationService.deleteLocation(locationToDelete.getId(), adminUser);

        // Then - Verify location was deleted from database
        boolean exists = locationRepository.existsById(locationToDelete.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void deleteLocation_ShouldThrowConflictException_WhenLocationHasChildren() {
        // Given - parentLocation has testLocation as a child
        
        // When/Then
        assertThatThrownBy(() -> locationService.deleteLocation(parentLocation.getId(), adminUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot delete location with child locations");
    }

    @Test
    void findAvailableLocations_ShouldReturnLocationsWithCapacity() {
        // Given - Create location at capacity
        Location atCapacityLocation = new Location();
        atCapacityLocation.setName("At Capacity Location");
        atCapacityLocation.setLocationType(LocationType.STORAGE);
        atCapacityLocation.setOrganization(testOrganization);
        atCapacityLocation.setMaxAssetCapacity(10);
        atCapacityLocation.setCurrentAssetCount(10); // At capacity
        atCapacityLocation.setActive(true);
        entityManager.persistAndFlush(atCapacityLocation);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<LocationSummaryDto> result = locationService.findAvailableLocations(testOrganization.getId(), pageable);

        // Then - Should return locations with available capacity
        assertThat(result.getContent()).hasSize(2); // parentLocation + testLocation (both have capacity)
        assertThat(result.getContent()).extracting(LocationSummaryDto::name)
                .containsExactlyInAnyOrder("Parent Location", "Integration Test Location");
        assertThat(result.getContent()).allMatch(loc -> 
                loc.currentAssetCount() < loc.maxAssetCapacity());
    }

    @Test
    void updateAssetCount_ShouldUpdateCountCorrectly() {
        // Given
        Long locationId = testLocation.getId();

        // When
        locationService.updateAssetCount(locationId);

        // Then
        Location updatedLocation = locationRepository.findById(locationId).orElse(null);
        assertThat(updatedLocation).isNotNull();
        assertThat(updatedLocation.getCurrentAssetCount()).isEqualTo(0); // No assets in test
    }

    @Test
    void updateAssetCount_ShouldThrowResourceNotFoundException_WhenLocationNotFound() {
        // When/Then
        assertThatThrownBy(() -> locationService.updateAssetCount(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Location not found with id: 999999");
    }

    @Test
    void endToEndWorkflow_ShouldWorkCorrectly() {
        // 1. Create a new location
        CreateLocationRequest createRequest = new CreateLocationRequest(
                "End-to-End Location", "Testing complete workflow", "FACTORY_FLOOR",
                "999 E2E St", "E2E City", "E2E Country", "E2E Building",
                "Ground", "Production Zone", new BigDecimal("45.5017"), new BigDecimal("-73.5673"),
                75, null
        );

        LocationDto createdLocation = locationService.createLocation(createRequest, adminUser);
        assertThat(createdLocation.name()).isEqualTo("End-to-End Location");

        // 2. Retrieve the created location
        LocationDto retrievedLocation = locationService.findLocationById(createdLocation.id(), inspectorUser);
        assertThat(retrievedLocation.name()).isEqualTo("End-to-End Location");

        // 3. Update the location
        UpdateLocationRequest updateRequest = new UpdateLocationRequest(
                "Updated E2E Location", "Updated workflow testing", "MAINTENANCE",
                "888 Updated E2E St", "Updated E2E City", "Updated E2E Country", "Updated E2E Building",
                "1", "Maintenance Zone", new BigDecimal("46.5197"), new BigDecimal("-72.5673"),
                100, true, null
        );

        LocationDto updatedLocation = locationService.updateLocation(createdLocation.id(), updateRequest, supervisorUser);
        assertThat(updatedLocation.name()).isEqualTo("Updated E2E Location");
        assertThat(updatedLocation.locationType()).isEqualTo("MAINTENANCE");

        // 4. Verify the location appears in the organization's location list
        Page<LocationSummaryDto> locations = locationService.findAllByOrganization(
                testOrganization.getId(), PageRequest.of(0, 10));
        assertThat(locations.getContent()).hasSize(3); // Original 2 + E2E location
        assertThat(locations.getContent()).extracting(LocationSummaryDto::name)
                .contains("Updated E2E Location");

        // 5. Finally, delete the location
        locationService.deleteLocation(createdLocation.id(), adminUser);

        // 6. Verify the location is no longer available
        assertThatThrownBy(() -> locationService.findLocationById(createdLocation.id(), inspectorUser))
                .isInstanceOf(ResourceNotFoundException.class);

        // 7. Verify the location is no longer in the list
        Page<LocationSummaryDto> finalLocations = locationService.findAllByOrganization(
                testOrganization.getId(), PageRequest.of(0, 10));
        assertThat(finalLocations.getContent()).hasSize(2); // Back to original 2
    }

    @Test
    void multiTenantIsolation_ShouldPreventCrossTenantAccess() {
        // When/Then - Other organization user should not be able to access our location
        assertThatThrownBy(() -> locationService.findLocationById(testLocation.getId(), otherOrgUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this location");

        // When/Then - Other organization user should not see our locations in their list
        Page<LocationSummaryDto> locations = locationService.findAllByOrganization(
                otherOrganization.getId(), PageRequest.of(0, 10));
        assertThat(locations.getContent()).isEmpty();
    }

    @Test 
    void locationHierarchy_ShouldMaintainParentChildRelationships() {
        // Given - testLocation is already a child of parentLocation

        // When - Find child locations of parent
        Page<LocationSummaryDto> parentLocations = locationService.findAllByOrganization(
                testOrganization.getId(), PageRequest.of(0, 10));

        // Then - Both locations should be present
        assertThat(parentLocations.getContent()).hasSize(2);

        // When - Get detailed info for child location
        LocationDto childDetails = locationService.findLocationById(testLocation.getId(), adminUser);

        // Then - Child should reference parent correctly
        assertThat(childDetails.parentLocationId()).isEqualTo(parentLocation.getId());
        assertThat(childDetails.parentLocationName()).isEqualTo("Parent Location");
    }
}