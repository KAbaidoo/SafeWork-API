package com.safework.api.domain.location.mapper;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.location.dto.LocationDto;
import com.safework.api.domain.location.dto.LocationSummaryDto;
import com.safework.api.domain.location.dto.LocationWithAssetsDto;
import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.location.model.LocationType;
import com.safework.api.domain.organization.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private LocationMapper locationMapper;
    private Location location;
    private Organization organization;
    private Location parentLocation;
    private Asset testAsset;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        locationMapper = new LocationMapper();
        
        testCreatedAt = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        testUpdatedAt = LocalDateTime.of(2023, 12, 1, 15, 45, 30);

        // Create organization
        organization = new Organization();
        organization.setName("Test Organization");
        setEntityId(organization, 1L);

        // Create parent location
        parentLocation = new Location();
        parentLocation.setName("Parent Location");
        parentLocation.setLocationType(LocationType.WAREHOUSE);
        parentLocation.setOrganization(organization);
        setEntityId(parentLocation, 2L);

        // Create test asset
        testAsset = new Asset();
        testAsset.setAssetTag("ASSET-001");
        testAsset.setName("Test Asset");
        testAsset.setStatus(AssetStatus.ACTIVE);
        setEntityId(testAsset, 1L);

        // Create location with all fields
        location = new Location();
        location.setName("Test Location");
        location.setDescription("Test location description");
        location.setLocationType(LocationType.FACTORY_FLOOR);
        location.setOrganization(organization);
        location.setAddress("123 Test Street");
        location.setCity("Test City");
        location.setCountry("Test Country");
        location.setBuildingName("Test Building");
        location.setFloor("2");
        location.setZone("Zone A");
        location.setLatitude(new BigDecimal("40.7128"));
        location.setLongitude(new BigDecimal("-74.0060"));
        location.setMaxAssetCapacity(100);
        location.setCurrentAssetCount(25);
        location.setActive(true);
        location.setParentLocation(parentLocation);
        location.setAssets(Arrays.asList(testAsset));
        
        // Set ID and timestamps using reflection
        setEntityId(location, 1L);
        setTimestamp(location, "createdAt", testCreatedAt);
        setTimestamp(location, "updatedAt", testUpdatedAt);
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
    void toDto_ShouldMapAllFields_WhenLocationHasAllFields() {
        // When
        LocationDto result = locationMapper.toDto(location);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Location");
        assertThat(result.description()).isEqualTo("Test location description");
        assertThat(result.locationType()).isEqualTo("FACTORY_FLOOR");
        assertThat(result.address()).isEqualTo("123 Test Street");
        assertThat(result.city()).isEqualTo("Test City");
        assertThat(result.country()).isEqualTo("Test Country");
        assertThat(result.buildingName()).isEqualTo("Test Building");
        assertThat(result.floor()).isEqualTo("2");
        assertThat(result.zone()).isEqualTo("Zone A");
        assertThat(result.latitude()).isEqualByComparingTo(new BigDecimal("40.7128"));
        assertThat(result.longitude()).isEqualByComparingTo(new BigDecimal("-74.0060"));
        assertThat(result.maxAssetCapacity()).isEqualTo(100);
        assertThat(result.currentAssetCount()).isEqualTo(25);
        assertThat(result.active()).isTrue();
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.parentLocationId()).isEqualTo(2L);
        assertThat(result.parentLocationName()).isEqualTo("Parent Location");
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
        assertThat(result.updatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    void toDto_ShouldMapRequiredFieldsOnly_WhenOptionalFieldsAreNull() {
        // Given
        Location minimalLocation = new Location();
        minimalLocation.setName("Minimal Location");
        minimalLocation.setLocationType(LocationType.OTHER);
        minimalLocation.setOrganization(organization);
        minimalLocation.setActive(true);
        setEntityId(minimalLocation, 2L);
        setTimestamp(minimalLocation, "createdAt", testCreatedAt);
        setTimestamp(minimalLocation, "updatedAt", testUpdatedAt);
        // description, address, city, country, building, floor, zone, coordinates, capacity, parent are null

        // When
        LocationDto result = locationMapper.toDto(minimalLocation);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Minimal Location");
        assertThat(result.description()).isNull();
        assertThat(result.locationType()).isEqualTo("OTHER");
        assertThat(result.address()).isNull();
        assertThat(result.city()).isNull();
        assertThat(result.country()).isNull();
        assertThat(result.buildingName()).isNull();
        assertThat(result.floor()).isNull();
        assertThat(result.zone()).isNull();
        assertThat(result.latitude()).isNull();
        assertThat(result.longitude()).isNull();
        assertThat(result.maxAssetCapacity()).isNull();
        assertThat(result.currentAssetCount()).isNull();
        assertThat(result.active()).isTrue();
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.parentLocationId()).isNull();
        assertThat(result.parentLocationName()).isNull();
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
        assertThat(result.updatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    void toDto_ShouldHandleNullOrganization() {
        // Given
        Location locationWithoutOrg = new Location();
        locationWithoutOrg.setName("Orphaned Location");
        locationWithoutOrg.setLocationType(LocationType.YARD);
        locationWithoutOrg.setParentLocation(parentLocation);
        locationWithoutOrg.setActive(true);
        setEntityId(locationWithoutOrg, 3L);
        setTimestamp(locationWithoutOrg, "createdAt", testCreatedAt);
        setTimestamp(locationWithoutOrg, "updatedAt", testUpdatedAt);

        // When
        LocationDto result = locationMapper.toDto(locationWithoutOrg);

        // Then
        assertThat(result.organizationId()).isNull();
        assertThat(result.parentLocationId()).isEqualTo(2L);
        assertThat(result.parentLocationName()).isEqualTo("Parent Location");
    }

    @Test
    void toDto_ShouldHandleNullParentLocation() {
        // Given
        Location rootLocation = new Location();
        rootLocation.setName("Root Location");
        rootLocation.setDescription("No parent location");
        rootLocation.setLocationType(LocationType.WAREHOUSE);
        rootLocation.setOrganization(organization);
        rootLocation.setMaxAssetCapacity(50);
        rootLocation.setCurrentAssetCount(10);
        rootLocation.setActive(true);
        setEntityId(rootLocation, 4L);
        setTimestamp(rootLocation, "createdAt", testCreatedAt);
        setTimestamp(rootLocation, "updatedAt", testUpdatedAt);

        // When
        LocationDto result = locationMapper.toDto(rootLocation);

        // Then
        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.name()).isEqualTo("Root Location");
        assertThat(result.description()).isEqualTo("No parent location");
        assertThat(result.locationType()).isEqualTo("WAREHOUSE");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.parentLocationId()).isNull();
        assertThat(result.parentLocationName()).isNull();
        assertThat(result.maxAssetCapacity()).isEqualTo(50);
        assertThat(result.currentAssetCount()).isEqualTo(10);
    }

    @Test
    void toDto_ShouldHandleNullLocationType() {
        // Given
        Location locationWithoutType = new Location();
        locationWithoutType.setName("No Type Location");
        locationWithoutType.setOrganization(organization);
        locationWithoutType.setActive(true);
        setEntityId(locationWithoutType, 5L);
        setTimestamp(locationWithoutType, "createdAt", testCreatedAt);
        setTimestamp(locationWithoutType, "updatedAt", testUpdatedAt);

        // When
        LocationDto result = locationMapper.toDto(locationWithoutType);

        // Then
        assertThat(result.locationType()).isNull();
        assertThat(result.name()).isEqualTo("No Type Location");
    }

    @Test
    void toSummaryDto_ShouldMapSummaryFields_WhenLocationHasAllFields() {
        // When
        LocationSummaryDto result = locationMapper.toSummaryDto(location);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Location");
        assertThat(result.locationType()).isEqualTo("FACTORY_FLOOR");
        assertThat(result.buildingName()).isEqualTo("Test Building");
        assertThat(result.floor()).isEqualTo("2");
        assertThat(result.zone()).isEqualTo("Zone A");
        assertThat(result.currentAssetCount()).isEqualTo(25);
        assertThat(result.maxAssetCapacity()).isEqualTo(100);
        assertThat(result.active()).isTrue();
    }

    @Test
    void toSummaryDto_ShouldMapRequiredFieldsOnly_WhenOptionalFieldsAreNull() {
        // Given
        Location minimalLocation = new Location();
        minimalLocation.setName("Summary Test Location");
        minimalLocation.setLocationType(LocationType.OFFICE);
        minimalLocation.setActive(false);
        setEntityId(minimalLocation, 3L);
        // building, floor, zone, asset counts are null

        // When
        LocationSummaryDto result = locationMapper.toSummaryDto(minimalLocation);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.name()).isEqualTo("Summary Test Location");
        assertThat(result.locationType()).isEqualTo("OFFICE");
        assertThat(result.buildingName()).isNull();
        assertThat(result.floor()).isNull();
        assertThat(result.zone()).isNull();
        assertThat(result.currentAssetCount()).isNull();
        assertThat(result.maxAssetCapacity()).isNull();
        assertThat(result.active()).isFalse();
    }

    @Test
    void toSummaryDto_ShouldHandleNullLocationType() {
        // Given
        Location noTypeLocation = new Location();
        noTypeLocation.setName("No Type Summary Location");
        noTypeLocation.setBuildingName("Building X");
        noTypeLocation.setFloor("3");
        noTypeLocation.setZone("Zone X");
        noTypeLocation.setCurrentAssetCount(5);
        noTypeLocation.setMaxAssetCapacity(20);
        noTypeLocation.setActive(true);
        setEntityId(noTypeLocation, 4L);

        // When
        LocationSummaryDto result = locationMapper.toSummaryDto(noTypeLocation);

        // Then
        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.name()).isEqualTo("No Type Summary Location");
        assertThat(result.locationType()).isNull();
        assertThat(result.buildingName()).isEqualTo("Building X");
        assertThat(result.floor()).isEqualTo("3");
        assertThat(result.zone()).isEqualTo("Zone X");
        assertThat(result.currentAssetCount()).isEqualTo(5);
        assertThat(result.maxAssetCapacity()).isEqualTo(20);
        assertThat(result.active()).isTrue();
    }

    @Test
    void toWithAssetsDto_ShouldMapLocationWithAssets_WhenLocationHasAssets() {
        // When
        LocationWithAssetsDto result = locationMapper.toWithAssetsDto(location);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Location");
        assertThat(result.locationType()).isEqualTo("FACTORY_FLOOR");
        assertThat(result.description()).isEqualTo("Test location description");
        assertThat(result.currentAssetCount()).isEqualTo(25);
        assertThat(result.maxAssetCapacity()).isEqualTo(100);
        assertThat(result.active()).isTrue();
        assertThat(result.assets()).hasSize(1);
        assertThat(result.assets().get(0).id()).isEqualTo(1L);
        assertThat(result.assets().get(0).assetTag()).isEqualTo("ASSET-001");
        assertThat(result.assets().get(0).name()).isEqualTo("Test Asset");
        assertThat(result.assets().get(0).status()).isEqualTo("ACTIVE");
    }

    @Test
    void toWithAssetsDto_ShouldMapLocationWithoutAssets_WhenLocationHasNoAssets() {
        // Given
        Location locationWithoutAssets = new Location();
        locationWithoutAssets.setName("Empty Location");
        locationWithoutAssets.setLocationType(LocationType.STORAGE);
        locationWithoutAssets.setDescription("Location with no assets");
        locationWithoutAssets.setCurrentAssetCount(0);
        locationWithoutAssets.setMaxAssetCapacity(50);
        locationWithoutAssets.setActive(true);
        locationWithoutAssets.setAssets(Collections.emptyList());
        setEntityId(locationWithoutAssets, 5L);

        // When
        LocationWithAssetsDto result = locationMapper.toWithAssetsDto(locationWithoutAssets);

        // Then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Empty Location");
        assertThat(result.locationType()).isEqualTo("STORAGE");
        assertThat(result.description()).isEqualTo("Location with no assets");
        assertThat(result.currentAssetCount()).isEqualTo(0);
        assertThat(result.maxAssetCapacity()).isEqualTo(50);
        assertThat(result.active()).isTrue();
        assertThat(result.assets()).isEmpty();
    }

    @Test
    void toWithAssetsDto_ShouldHandleNullAssets() {
        // Given
        Location locationWithNullAssets = new Location();
        locationWithNullAssets.setName("Null Assets Location");
        locationWithNullAssets.setLocationType(LocationType.MAINTENANCE);
        locationWithNullAssets.setActive(true);
        locationWithNullAssets.setAssets(null);
        setEntityId(locationWithNullAssets, 6L);

        // When
        LocationWithAssetsDto result = locationMapper.toWithAssetsDto(locationWithNullAssets);

        // Then
        assertThat(result.id()).isEqualTo(6L);
        assertThat(result.name()).isEqualTo("Null Assets Location");
        assertThat(result.locationType()).isEqualTo("MAINTENANCE");
        assertThat(result.assets()).isNull();
    }

    @Test
    void toWithAssetsDto_ShouldHandleAssetsWithNullStatus() {
        // Given
        Asset assetWithNullStatus = new Asset();
        assetWithNullStatus.setAssetTag("ASSET-NULL");
        assetWithNullStatus.setName("Asset Without Status");
        assetWithNullStatus.setStatus(null);
        setEntityId(assetWithNullStatus, 2L);

        Location locationWithNullStatusAsset = new Location();
        locationWithNullStatusAsset.setName("Location with Null Status Asset");
        locationWithNullStatusAsset.setLocationType(LocationType.YARD);
        locationWithNullStatusAsset.setActive(true);
        locationWithNullStatusAsset.setAssets(Arrays.asList(assetWithNullStatus));
        setEntityId(locationWithNullStatusAsset, 7L);

        // When
        LocationWithAssetsDto result = locationMapper.toWithAssetsDto(locationWithNullStatusAsset);

        // Then
        assertThat(result.assets()).hasSize(1);
        assertThat(result.assets().get(0).id()).isEqualTo(2L);
        assertThat(result.assets().get(0).assetTag()).isEqualTo("ASSET-NULL");
        assertThat(result.assets().get(0).name()).isEqualTo("Asset Without Status");
        assertThat(result.assets().get(0).status()).isNull();
    }

    @Test
    void mappingConsistency_BothDtoMethodsShouldMapCommonFieldsIdentically() {
        // When
        LocationDto fullDto = locationMapper.toDto(location);
        LocationSummaryDto summaryDto = locationMapper.toSummaryDto(location);
        LocationWithAssetsDto withAssetsDto = locationMapper.toWithAssetsDto(location);

        // Then - Common fields should be identical
        assertThat(fullDto.id()).isEqualTo(summaryDto.id());
        assertThat(fullDto.name()).isEqualTo(summaryDto.name());
        assertThat(fullDto.locationType()).isEqualTo(summaryDto.locationType());
        assertThat(fullDto.buildingName()).isEqualTo(summaryDto.buildingName());
        assertThat(fullDto.floor()).isEqualTo(summaryDto.floor());
        assertThat(fullDto.zone()).isEqualTo(summaryDto.zone());
        assertThat(fullDto.currentAssetCount()).isEqualTo(summaryDto.currentAssetCount());
        assertThat(fullDto.maxAssetCapacity()).isEqualTo(summaryDto.maxAssetCapacity());
        assertThat(fullDto.active()).isEqualTo(summaryDto.active());

        // Common fields between full and with assets DTO
        assertThat(fullDto.id()).isEqualTo(withAssetsDto.id());
        assertThat(fullDto.name()).isEqualTo(withAssetsDto.name());
        assertThat(fullDto.locationType()).isEqualTo(withAssetsDto.locationType());
        assertThat(fullDto.description()).isEqualTo(withAssetsDto.description());
        assertThat(fullDto.currentAssetCount()).isEqualTo(withAssetsDto.currentAssetCount());
        assertThat(fullDto.maxAssetCapacity()).isEqualTo(withAssetsDto.maxAssetCapacity());
        assertThat(fullDto.active()).isEqualTo(withAssetsDto.active());
    }

    @Test
    void toDto_ShouldHandleSpecialCharactersInFields() {
        // Given
        location.setName("Location & Storage (Area #1)");
        location.setDescription("Special chars: @#$%^&*()");
        location.setAddress("123 Main St. & Co.");
        location.setCity("New York & Queens");
        location.setCountry("USA & Territories");
        location.setBuildingName("Building A & B");
        location.setZone("Zone A-1 & A-2");

        // When
        LocationDto result = locationMapper.toDto(location);

        // Then
        assertThat(result.name()).isEqualTo("Location & Storage (Area #1)");
        assertThat(result.description()).isEqualTo("Special chars: @#$%^&*()");
        assertThat(result.address()).isEqualTo("123 Main St. & Co.");
        assertThat(result.city()).isEqualTo("New York & Queens");
        assertThat(result.country()).isEqualTo("USA & Territories");
        assertThat(result.buildingName()).isEqualTo("Building A & B");
        assertThat(result.zone()).isEqualTo("Zone A-1 & A-2");
    }

    @Test
    void toSummaryDto_ShouldHandleSpecialCharactersInFields() {
        // Given
        location.setName("Summary & Location");
        location.setBuildingName("Building & Complex");
        location.setFloor("1st & 2nd");
        location.setZone("Zone & Area");

        // When
        LocationSummaryDto result = locationMapper.toSummaryDto(location);

        // Then
        assertThat(result.name()).isEqualTo("Summary & Location");
        assertThat(result.buildingName()).isEqualTo("Building & Complex");
        assertThat(result.floor()).isEqualTo("1st & 2nd");
        assertThat(result.zone()).isEqualTo("Zone & Area");
    }

    @Test
    void toDto_ShouldHandleEmptyStringsAsValues() {
        // Given
        location.setDescription("");
        location.setAddress("");
        location.setCity("");
        location.setCountry("");
        location.setBuildingName("");
        location.setFloor("");
        location.setZone("");

        // When
        LocationDto result = locationMapper.toDto(location);

        // Then
        assertThat(result.description()).isEqualTo("");
        assertThat(result.address()).isEqualTo("");
        assertThat(result.city()).isEqualTo("");
        assertThat(result.country()).isEqualTo("");
        assertThat(result.buildingName()).isEqualTo("");
        assertThat(result.floor()).isEqualTo("");
        assertThat(result.zone()).isEqualTo("");
    }

    @Test
    void toSummaryDto_ShouldHandleEmptyStrings() {
        // Given
        location.setBuildingName("");
        location.setFloor("");
        location.setZone("");

        // When
        LocationSummaryDto result = locationMapper.toSummaryDto(location);

        // Then
        assertThat(result.buildingName()).isEqualTo("");
        assertThat(result.floor()).isEqualTo("");
        assertThat(result.zone()).isEqualTo("");
    }

    @Test
    void toDto_ShouldHandleLargeCapacityValues() {
        // Given
        location.setMaxAssetCapacity(Integer.MAX_VALUE);
        location.setCurrentAssetCount(Integer.MAX_VALUE - 1);

        // When
        LocationDto result = locationMapper.toDto(location);

        // Then
        assertThat(result.maxAssetCapacity()).isEqualTo(Integer.MAX_VALUE);
        assertThat(result.currentAssetCount()).isEqualTo(Integer.MAX_VALUE - 1);
    }

    @Test
    void toSummaryDto_ShouldHandleLargeCapacityValues() {
        // Given
        location.setMaxAssetCapacity(Integer.MAX_VALUE);
        location.setCurrentAssetCount(Integer.MAX_VALUE - 1);

        // When
        LocationSummaryDto result = locationMapper.toSummaryDto(location);

        // Then
        assertThat(result.maxAssetCapacity()).isEqualTo(Integer.MAX_VALUE);
        assertThat(result.currentAssetCount()).isEqualTo(Integer.MAX_VALUE - 1);
    }

    @Test
    void mappingPerformance_ShouldHandleLargeDescriptionsAndNames() {
        // Given
        String longName = "A".repeat(100); // 100 character name
        String longDescription = "B".repeat(500); // 500 character description
        String longAddress = "C".repeat(200); // 200 character address

        location.setName(longName);
        location.setDescription(longDescription);
        location.setAddress(longAddress);

        // When
        LocationDto fullDto = locationMapper.toDto(location);
        LocationSummaryDto summaryDto = locationMapper.toSummaryDto(location);
        LocationWithAssetsDto withAssetsDto = locationMapper.toWithAssetsDto(location);

        // Then
        assertThat(fullDto.name()).isEqualTo(longName);
        assertThat(fullDto.description()).isEqualTo(longDescription);
        assertThat(fullDto.address()).isEqualTo(longAddress);

        assertThat(summaryDto.name()).isEqualTo(longName);
        
        assertThat(withAssetsDto.name()).isEqualTo(longName);
        assertThat(withAssetsDto.description()).isEqualTo(longDescription);
    }

    @Test
    void toDto_ShouldHandleExtremeCoordinateValues() {
        // Given
        location.setLatitude(new BigDecimal("90.00000000")); // North pole
        location.setLongitude(new BigDecimal("-180.00000000")); // International date line

        // When
        LocationDto result = locationMapper.toDto(location);

        // Then
        assertThat(result.latitude()).isEqualByComparingTo(new BigDecimal("90.00000000"));
        assertThat(result.longitude()).isEqualByComparingTo(new BigDecimal("-180.00000000"));
    }
}