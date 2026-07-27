package com.safework.api.domain.location.repository;

import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.location.model.LocationType;
import com.safework.api.domain.organization.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class LocationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LocationRepository locationRepository;

    private Organization organization1;
    private Organization organization2;
    private Location parentLocation;
    private Location childLocation1;
    private Location childLocation2;
    private Location warehouseLocation;

    @BeforeEach
    void setUp() {
        // Create test organizations
        organization1 = new Organization();
        organization1.setName("Tech Logistics Corp");
        entityManager.persistAndFlush(organization1);

        organization2 = new Organization();
        organization2.setName("Manufacturing Inc");
        entityManager.persistAndFlush(organization2);

        // Create parent location (main warehouse)
        parentLocation = new Location();
        parentLocation.setName("Main Warehouse");
        parentLocation.setDescription("Primary storage facility");
        parentLocation.setLocationType(LocationType.WAREHOUSE);
        parentLocation.setOrganization(organization1);
        parentLocation.setAddress("123 Industrial Blvd");
        parentLocation.setCity("Industrial City");
        parentLocation.setCountry("USA");
        parentLocation.setBuildingName("Building A");
        parentLocation.setMaxAssetCapacity(100);
        parentLocation.setCurrentAssetCount(25);
        parentLocation.setActive(true);
        parentLocation.setLatitude(new BigDecimal("40.7128"));
        parentLocation.setLongitude(new BigDecimal("-74.0060"));
        entityManager.persistAndFlush(parentLocation);

        // Create child locations
        childLocation1 = new Location();
        childLocation1.setName("Section A1");
        childLocation1.setDescription("Electronics storage section");
        childLocation1.setLocationType(LocationType.STORAGE);
        childLocation1.setOrganization(organization1);
        childLocation1.setParentLocation(parentLocation);
        childLocation1.setFloor("1");
        childLocation1.setZone("A1");
        childLocation1.setMaxAssetCapacity(50);
        childLocation1.setCurrentAssetCount(15);
        childLocation1.setActive(true);
        entityManager.persistAndFlush(childLocation1);

        childLocation2 = new Location();
        childLocation2.setName("Section B1");
        childLocation2.setDescription("Heavy machinery section");
        childLocation2.setLocationType(LocationType.STORAGE);
        childLocation2.setOrganization(organization1);
        childLocation2.setParentLocation(parentLocation);
        childLocation2.setFloor("1");
        childLocation2.setZone("B1");
        childLocation2.setMaxAssetCapacity(30);
        childLocation2.setCurrentAssetCount(30);  // At capacity
        childLocation2.setActive(true);
        entityManager.persistAndFlush(childLocation2);

        // Create standalone warehouse in org1
        warehouseLocation = new Location();
        warehouseLocation.setName("Secondary Warehouse");
        warehouseLocation.setDescription("Backup storage facility");
        warehouseLocation.setLocationType(LocationType.WAREHOUSE);
        warehouseLocation.setOrganization(organization1);
        warehouseLocation.setAddress("456 Storage Ave");
        warehouseLocation.setCity("Storage City");
        warehouseLocation.setCountry("USA");
        warehouseLocation.setMaxAssetCapacity(75);
        warehouseLocation.setCurrentAssetCount(10);
        warehouseLocation.setActive(true);
        entityManager.persistAndFlush(warehouseLocation);

        entityManager.clear();
    }

    @Test
    void findAllByOrganizationId_ShouldReturnPaginatedLocations_WhenOrganizationExists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Location> result = locationRepository.findAllByOrganizationId(organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).extracting(Location::getName)
                .containsExactlyInAnyOrder("Main Warehouse", "Section A1", "Section B1", "Secondary Warehouse");
    }

    @Test
    void findAllByOrganizationId_ShouldReturnEmpty_WhenOrganizationHasNoLocations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Location> result = locationRepository.findAllByOrganizationId(organization2.getId(), pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findAllByOrganizationId_ShouldRespectPagination() {
        // Given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // When
        Page<Location> firstResult = locationRepository.findAllByOrganizationId(organization1.getId(), firstPage);
        Page<Location> secondResult = locationRepository.findAllByOrganizationId(organization1.getId(), secondPage);

        // Then
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(firstResult.getTotalElements()).isEqualTo(4);
        assertThat(firstResult.hasNext()).isTrue();

        assertThat(secondResult.getContent()).hasSize(2);
        assertThat(secondResult.getTotalElements()).isEqualTo(4);
        assertThat(secondResult.hasNext()).isFalse();
    }

    @Test
    void findAllByOrganizationIdAndActiveTrue_ShouldReturnOnlyActiveLocations() {
        // Given - Create inactive location
        Location inactiveLocation = new Location();
        inactiveLocation.setName("Inactive Location");
        inactiveLocation.setLocationType(LocationType.OFFICE);
        inactiveLocation.setOrganization(organization1);
        inactiveLocation.setActive(false);
        entityManager.persistAndFlush(inactiveLocation);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Location> result = locationRepository.findAllByOrganizationIdAndActiveTrue(organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(4); // Only active locations
        assertThat(result.getContent()).extracting(Location::getName)
                .doesNotContain("Inactive Location");
        assertThat(result.getContent()).allMatch(Location::getActive);
    }

    @Test
    void findByOrganizationIdAndName_ShouldReturnLocation_WhenExists() {
        // When
        Optional<Location> result = locationRepository.findByOrganizationIdAndName(
                organization1.getId(), "Main Warehouse");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Main Warehouse");
        assertThat(result.get().getDescription()).isEqualTo("Primary storage facility");
        assertThat(result.get().getLocationType()).isEqualTo(LocationType.WAREHOUSE);
        assertThat(result.get().getAddress()).isEqualTo("123 Industrial Blvd");
        assertThat(result.get().getCity()).isEqualTo("Industrial City");
        assertThat(result.get().getMaxAssetCapacity()).isEqualTo(100);
        assertThat(result.get().getCurrentAssetCount()).isEqualTo(25);
    }

    @Test
    void findByOrganizationIdAndName_ShouldReturnEmpty_WhenNotFound() {
        // When
        Optional<Location> result = locationRepository.findByOrganizationIdAndName(
                organization1.getId(), "Non-existent Location");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByOrganizationIdAndName_ShouldReturnEmpty_WhenDifferentOrganization() {
        // When
        Optional<Location> result = locationRepository.findByOrganizationIdAndName(
                organization2.getId(), "Main Warehouse");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByOrganizationIdAndName_ShouldReturnTrue_WhenExists() {
        // When
        boolean result = locationRepository.existsByOrganizationIdAndName(
                organization1.getId(), "Main Warehouse");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByOrganizationIdAndName_ShouldReturnFalse_WhenNotExists() {
        // When
        boolean result = locationRepository.existsByOrganizationIdAndName(
                organization1.getId(), "Non-existent Location");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByOrganizationIdAndName_ShouldReturnFalse_WhenDifferentOrganization() {
        // When
        boolean result = locationRepository.existsByOrganizationIdAndName(
                organization2.getId(), "Main Warehouse");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void findByParentLocationId_ShouldReturnChildLocations() {
        // When
        List<Location> result = locationRepository.findByParentLocationId(parentLocation.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Location::getName)
                .containsExactlyInAnyOrder("Section A1", "Section B1");
        assertThat(result).allMatch(child -> child.getParentLocation().getId().equals(parentLocation.getId()));
    }

    @Test
    void findByParentLocationId_ShouldReturnEmpty_WhenNoChildren() {
        // When
        List<Location> result = locationRepository.findByParentLocationId(childLocation1.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByParentLocationIdWithPagination_ShouldReturnPaginatedChildren() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Location> result = locationRepository.findByParentLocationId(parentLocation.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void findByOrganizationIdAndParentLocationIsNull_ShouldReturnRootLocations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Location> result = locationRepository.findByOrganizationIdAndParentLocationIsNull(
                organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(2); // parentLocation and warehouseLocation
        assertThat(result.getContent()).extracting(Location::getName)
                .containsExactlyInAnyOrder("Main Warehouse", "Secondary Warehouse");
        assertThat(result.getContent()).allMatch(loc -> loc.getParentLocation() == null);
    }

    @Test
    void countAssetsByLocationId_ShouldReturnCorrectCount() {
        // Note: This test would require Asset entities, but we'll test the query structure
        // When
        long result = locationRepository.countAssetsByLocationId(parentLocation.getId());

        // Then
        assertThat(result).isEqualTo(0); // No assets created in test setup
    }

    @Test
    void findAvailableLocationsByOrganizationId_ShouldReturnLocationsWithCapacity() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Location> result = locationRepository.findAvailableLocationsByOrganizationId(
                organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(3); // All except childLocation2 which is at capacity
        assertThat(result.getContent()).extracting(Location::getName)
                .containsExactlyInAnyOrder("Main Warehouse", "Section A1", "Secondary Warehouse");
        assertThat(result.getContent()).allMatch(loc -> 
                loc.getCurrentAssetCount() < loc.getMaxAssetCapacity());
    }

    @Test
    void findByOrganizationIdAndLocationType_ShouldReturnLocationsByType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Location> result = locationRepository.findByOrganizationIdAndLocationType(
                organization1.getId(), LocationType.WAREHOUSE, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Location::getName)
                .containsExactlyInAnyOrder("Main Warehouse", "Secondary Warehouse");
        assertThat(result.getContent()).allMatch(loc -> 
                loc.getLocationType() == LocationType.WAREHOUSE);
    }

    @Test
    void findByOrganizationIdAndLocationType_ShouldReturnEmpty_WhenNoMatchingType() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Location> result = locationRepository.findByOrganizationIdAndLocationType(
                organization1.getId(), LocationType.OFFICE, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void save_ShouldPersistLocation_WithAllFields() {
        // Given
        Location newLocation = new Location();
        newLocation.setName("Loading Dock A");
        newLocation.setDescription("Primary loading and unloading area");
        newLocation.setLocationType(LocationType.LOADING_DOCK);
        newLocation.setOrganization(organization1);
        newLocation.setAddress("789 Shipping Lane");
        newLocation.setCity("Port City");
        newLocation.setCountry("USA");
        newLocation.setBuildingName("Dock Building");
        newLocation.setFloor("Ground");
        newLocation.setZone("Dock A");
        newLocation.setLatitude(new BigDecimal("41.8781"));
        newLocation.setLongitude(new BigDecimal("-87.6298"));
        newLocation.setMaxAssetCapacity(20);
        newLocation.setCurrentAssetCount(5);
        newLocation.setActive(true);

        // When
        Location savedLocation = locationRepository.save(newLocation);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedLocation.getId()).isNotNull();
        assertThat(savedLocation.getCreatedAt()).isNotNull();
        assertThat(savedLocation.getUpdatedAt()).isNotNull();

        // Verify it can be found
        Optional<Location> foundLocation = locationRepository.findById(savedLocation.getId());
        assertThat(foundLocation).isPresent();
        assertThat(foundLocation.get().getName()).isEqualTo("Loading Dock A");
        assertThat(foundLocation.get().getDescription()).isEqualTo("Primary loading and unloading area");
        assertThat(foundLocation.get().getLocationType()).isEqualTo(LocationType.LOADING_DOCK);
        assertThat(foundLocation.get().getAddress()).isEqualTo("789 Shipping Lane");
        assertThat(foundLocation.get().getCity()).isEqualTo("Port City");
        assertThat(foundLocation.get().getCountry()).isEqualTo("USA");
        assertThat(foundLocation.get().getBuildingName()).isEqualTo("Dock Building");
        assertThat(foundLocation.get().getFloor()).isEqualTo("Ground");
        assertThat(foundLocation.get().getZone()).isEqualTo("Dock A");
        assertThat(foundLocation.get().getLatitude()).isEqualByComparingTo(new BigDecimal("41.8781"));
        assertThat(foundLocation.get().getLongitude()).isEqualByComparingTo(new BigDecimal("-87.6298"));
        assertThat(foundLocation.get().getMaxAssetCapacity()).isEqualTo(20);
        assertThat(foundLocation.get().getCurrentAssetCount()).isEqualTo(5);
        assertThat(foundLocation.get().getActive()).isTrue();
    }

    @Test
    void save_ShouldPersistLocation_WithMinimalFields() {
        // Given
        Location minimalLocation = new Location();
        minimalLocation.setName("Minimal Location");
        minimalLocation.setLocationType(LocationType.OTHER);
        minimalLocation.setOrganization(organization1);
        minimalLocation.setActive(true);

        // When
        Location savedLocation = locationRepository.save(minimalLocation);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Location> foundLocation = locationRepository.findById(savedLocation.getId());
        assertThat(foundLocation).isPresent();
        assertThat(foundLocation.get().getName()).isEqualTo("Minimal Location");
        assertThat(foundLocation.get().getDescription()).isNull();
        assertThat(foundLocation.get().getAddress()).isNull();
        assertThat(foundLocation.get().getCity()).isNull();
        assertThat(foundLocation.get().getCountry()).isNull();
        assertThat(foundLocation.get().getBuildingName()).isNull();
        assertThat(foundLocation.get().getFloor()).isNull();
        assertThat(foundLocation.get().getZone()).isNull();
        assertThat(foundLocation.get().getLatitude()).isNull();
        assertThat(foundLocation.get().getLongitude()).isNull();
        assertThat(foundLocation.get().getMaxAssetCapacity()).isNull();
        assertThat(foundLocation.get().getCurrentAssetCount()).isNull();
        assertThat(foundLocation.get().getParentLocation()).isNull();
    }

    @Test
    void save_ShouldThrowException_WhenNameIsNull() {
        // Given
        Location invalidLocation = new Location();
        invalidLocation.setLocationType(LocationType.WAREHOUSE);
        invalidLocation.setOrganization(organization1);

        // When/Then
        assertThatThrownBy(() -> {
            locationRepository.save(invalidLocation);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldThrowException_WhenLocationTypeIsNull() {
        // Given
        Location invalidLocation = new Location();
        invalidLocation.setName("Invalid Location");
        invalidLocation.setOrganization(organization1);

        // When/Then
        assertThatThrownBy(() -> {
            locationRepository.save(invalidLocation);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldThrowException_WhenOrganizationIsNull() {
        // Given
        Location invalidLocation = new Location();
        invalidLocation.setName("Invalid Location");
        invalidLocation.setLocationType(LocationType.WAREHOUSE);

        // When/Then
        assertThatThrownBy(() -> {
            locationRepository.save(invalidLocation);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateNameInSameOrganization() {
        // Given
        Location duplicateLocation = new Location();
        duplicateLocation.setName("Main Warehouse"); // Same as parentLocation
        duplicateLocation.setLocationType(LocationType.WAREHOUSE);
        duplicateLocation.setOrganization(organization1);
        duplicateLocation.setActive(true);

        // When/Then
        assertThatThrownBy(() -> {
            locationRepository.save(duplicateLocation);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldAllowSameName_InDifferentOrganizations() {
        // Given
        Location sameName = new Location();
        sameName.setName("Main Warehouse"); // Same name as in org1
        sameName.setLocationType(LocationType.WAREHOUSE);
        sameName.setOrganization(organization2); // Different organization
        sameName.setActive(true);

        // When
        Location savedLocation = locationRepository.save(sameName);
        entityManager.flush();

        // Then
        assertThat(savedLocation.getId()).isNotNull();
        assertThat(savedLocation.getName()).isEqualTo("Main Warehouse");
        assertThat(savedLocation.getOrganization().getId()).isEqualTo(organization2.getId());
    }

    @Test
    void locationWithParent_ShouldMaintainHierarchyRelationships() {
        // When
        Location loadedChild = locationRepository.findById(childLocation1.getId()).get();

        // Then
        assertThat(loadedChild.getParentLocation()).isNotNull();
        assertThat(loadedChild.getParentLocation().getId()).isEqualTo(parentLocation.getId());
        assertThat(loadedChild.getParentLocation().getName()).isEqualTo("Main Warehouse");
    }

    @Test
    void locationWithChildren_ShouldMaintainChildRelationships() {
        // When
        Location loadedParent = locationRepository.findById(parentLocation.getId()).get();

        // Then
        assertThat(loadedParent.getChildLocations()).hasSize(2);
        assertThat(loadedParent.getChildLocations()).extracting(Location::getName)
                .containsExactlyInAnyOrder("Section A1", "Section B1");
    }

    @Test
    void update_ShouldUpdateTimestamp() throws InterruptedException {
        // Given
        Location location = locationRepository.findById(parentLocation.getId()).get();
        var originalUpdatedAt = location.getUpdatedAt();

        Thread.sleep(100); // Ensure time difference

        // When
        location.setDescription("Updated description");
        Location updatedLocation = locationRepository.save(location);
        entityManager.flush();

        // Then
        assertThat(updatedLocation.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedLocation.getCreatedAt()).isEqualTo(location.getCreatedAt());
    }

    @Test
    void delete_ShouldRemoveLocation() {
        // Given
        Long locationId = warehouseLocation.getId();
        assertThat(locationRepository.findById(locationId)).isPresent();

        // When
        locationRepository.delete(warehouseLocation);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(locationRepository.findById(locationId)).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllLocations() {
        // When
        List<Location> locations = locationRepository.findAll();

        // Then
        assertThat(locations).hasSize(4);
        assertThat(locations).extracting(Location::getName)
                .containsExactlyInAnyOrder("Main Warehouse", "Section A1", "Section B1", "Secondary Warehouse");
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = locationRepository.count();

        // Then
        assertThat(count).isEqualTo(4);
    }

    @Test
    void locationTypeEnum_ShouldPersistAndRetrieveCorrectly() {
        // Given
        Location officeLocation = new Location();
        officeLocation.setName("Head Office");
        officeLocation.setLocationType(LocationType.OFFICE);
        officeLocation.setOrganization(organization1);
        officeLocation.setActive(true);

        // When
        Location saved = locationRepository.save(officeLocation);
        entityManager.flush();
        entityManager.clear();

        // Then
        Location loaded = locationRepository.findById(saved.getId()).get();
        assertThat(loaded.getLocationType()).isEqualTo(LocationType.OFFICE);
    }

    @Test
    void coordinates_ShouldPersistWithCorrectPrecision() {
        // Given
        Location preciseLocation = new Location();
        preciseLocation.setName("Precise Location");
        preciseLocation.setLocationType(LocationType.YARD);
        preciseLocation.setOrganization(organization1);
        preciseLocation.setLatitude(new BigDecimal("40.12345678"));
        preciseLocation.setLongitude(new BigDecimal("-74.98765432"));
        preciseLocation.setActive(true);

        // When
        Location saved = locationRepository.save(preciseLocation);
        entityManager.flush();
        entityManager.clear();

        // Then
        Location loaded = locationRepository.findById(saved.getId()).get();
        assertThat(loaded.getLatitude()).isEqualByComparingTo(new BigDecimal("40.12345678"));
        assertThat(loaded.getLongitude()).isEqualByComparingTo(new BigDecimal("-74.98765432"));
    }
}