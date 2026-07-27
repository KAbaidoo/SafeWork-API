package com.safework.api.domain.organization.mapper;

import com.safework.api.domain.organization.dto.OrganizationDto;
import com.safework.api.domain.organization.dto.OrganizationSummaryDto;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.model.OrganizationSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationMapperTest {

    private OrganizationMapper organizationMapper;
    private Organization organization;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        organizationMapper = new OrganizationMapper();
        
        testCreatedAt = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        testUpdatedAt = LocalDateTime.of(2023, 12, 1, 15, 45, 30);

        organization = new Organization();
        organization.setName("Test Organization");
        organization.setAddress("123 Test Street, Test City, TC 12345");
        organization.setPhone("+1-555-0123");
        organization.setWebsite("https://testorg.com");
        organization.setIndustry("Technology");
        organization.setSize(OrganizationSize.MEDIUM);
        
        // Set ID and timestamps using reflection since they have protected setters
        setEntityId(organization, 1L);
        setTimestamp(organization, "createdAt", testCreatedAt);
        setTimestamp(organization, "updatedAt", testUpdatedAt);
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
    void toDto_ShouldMapAllFields_WhenOrganizationHasAllFields() {
        // When
        OrganizationDto result = organizationMapper.toDto(organization);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Organization");
        assertThat(result.address()).isEqualTo("123 Test Street, Test City, TC 12345");
        assertThat(result.phone()).isEqualTo("+1-555-0123");
        assertThat(result.website()).isEqualTo("https://testorg.com");
        assertThat(result.industry()).isEqualTo("Technology");
        assertThat(result.size()).isEqualTo("MEDIUM");
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
        assertThat(result.updatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    void toDto_ShouldMapRequiredFieldsOnly_WhenOptionalFieldsAreNull() {
        // Given
        Organization minimalOrg = new Organization();
        minimalOrg.setName("Minimal Organization");
        setEntityId(minimalOrg, 2L);
        setTimestamp(minimalOrg, "createdAt", testCreatedAt);
        setTimestamp(minimalOrg, "updatedAt", testUpdatedAt);
        // All other fields are null

        // When
        OrganizationDto result = organizationMapper.toDto(minimalOrg);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Minimal Organization");
        assertThat(result.address()).isNull();
        assertThat(result.phone()).isNull();
        assertThat(result.website()).isNull();
        assertThat(result.industry()).isNull();
        assertThat(result.size()).isNull();
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
        assertThat(result.updatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    void toDto_ShouldMapAllOrganizationSizes() {
        // Test SMALL
        organization.setSize(OrganizationSize.SMALL);
        OrganizationDto smallResult = organizationMapper.toDto(organization);
        assertThat(smallResult.size()).isEqualTo("SMALL");

        // Test MEDIUM
        organization.setSize(OrganizationSize.MEDIUM);
        OrganizationDto mediumResult = organizationMapper.toDto(organization);
        assertThat(mediumResult.size()).isEqualTo("MEDIUM");

        // Test LARGE
        organization.setSize(OrganizationSize.LARGE);
        OrganizationDto largeResult = organizationMapper.toDto(organization);
        assertThat(largeResult.size()).isEqualTo("LARGE");

        // Test ENTERPRISE
        organization.setSize(OrganizationSize.ENTERPRISE);
        OrganizationDto enterpriseResult = organizationMapper.toDto(organization);
        assertThat(enterpriseResult.size()).isEqualTo("ENTERPRISE");
    }

    @Test
    void toDto_ShouldHandleNullSize() {
        // Given
        organization.setSize(null);

        // When
        OrganizationDto result = organizationMapper.toDto(organization);

        // Then
        assertThat(result.size()).isNull();
        // Verify other fields are still mapped correctly
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Organization");
    }

    @Test
    void toSummaryDto_ShouldMapSummaryFields_WhenOrganizationHasAllFields() {
        // When
        OrganizationSummaryDto result = organizationMapper.toSummaryDto(organization);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Organization");
        assertThat(result.industry()).isEqualTo("Technology");
        assertThat(result.size()).isEqualTo("MEDIUM");
    }

    @Test
    void toSummaryDto_ShouldMapRequiredFieldsOnly_WhenOptionalFieldsAreNull() {
        // Given
        Organization minimalOrg = new Organization();
        minimalOrg.setName("Summary Test Organization");
        setEntityId(minimalOrg, 3L);
        // industry and size are null

        // When
        OrganizationSummaryDto result = organizationMapper.toSummaryDto(minimalOrg);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.name()).isEqualTo("Summary Test Organization");
        assertThat(result.industry()).isNull();
        assertThat(result.size()).isNull();
    }

    @Test
    void toSummaryDto_ShouldMapAllOrganizationSizes() {
        // Test SMALL
        organization.setSize(OrganizationSize.SMALL);
        OrganizationSummaryDto smallResult = organizationMapper.toSummaryDto(organization);
        assertThat(smallResult.size()).isEqualTo("SMALL");

        // Test MEDIUM
        organization.setSize(OrganizationSize.MEDIUM);
        OrganizationSummaryDto mediumResult = organizationMapper.toSummaryDto(organization);
        assertThat(mediumResult.size()).isEqualTo("MEDIUM");

        // Test LARGE
        organization.setSize(OrganizationSize.LARGE);
        OrganizationSummaryDto largeResult = organizationMapper.toSummaryDto(organization);
        assertThat(largeResult.size()).isEqualTo("LARGE");

        // Test ENTERPRISE
        organization.setSize(OrganizationSize.ENTERPRISE);
        OrganizationSummaryDto enterpriseResult = organizationMapper.toSummaryDto(organization);
        assertThat(enterpriseResult.size()).isEqualTo("ENTERPRISE");
    }

    @Test
    void toSummaryDto_ShouldHandleNullSize() {
        // Given
        organization.setSize(null);

        // When
        OrganizationSummaryDto result = organizationMapper.toSummaryDto(organization);

        // Then
        assertThat(result.size()).isNull();
        // Verify other fields are still mapped correctly
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Organization");
        assertThat(result.industry()).isEqualTo("Technology");
    }

    @Test
    void mappingConsistency_BothMethodsShouldMapCommonFieldsIdentically() {
        // When
        OrganizationDto fullDto = organizationMapper.toDto(organization);
        OrganizationSummaryDto summaryDto = organizationMapper.toSummaryDto(organization);

        // Then - Common fields should be identical
        assertThat(fullDto.id()).isEqualTo(summaryDto.id());
        assertThat(fullDto.name()).isEqualTo(summaryDto.name());
        assertThat(fullDto.industry()).isEqualTo(summaryDto.industry());
        assertThat(fullDto.size()).isEqualTo(summaryDto.size());
    }

    @Test
    void toDto_ShouldHandleSpecialCharactersInFields() {
        // Given
        organization.setName("Test Org & Co. (Holdings) Ltd.");
        organization.setAddress("123 Main St., Apt. #456, City & County, ST 12345-6789");
        organization.setPhone("+1 (555) 123-4567 ext. 890");
        organization.setWebsite("https://test-org.co.uk/path?param=value&other=123");
        organization.setIndustry("Research & Development");

        // When
        OrganizationDto result = organizationMapper.toDto(organization);

        // Then
        assertThat(result.name()).isEqualTo("Test Org & Co. (Holdings) Ltd.");
        assertThat(result.address()).isEqualTo("123 Main St., Apt. #456, City & County, ST 12345-6789");
        assertThat(result.phone()).isEqualTo("+1 (555) 123-4567 ext. 890");
        assertThat(result.website()).isEqualTo("https://test-org.co.uk/path?param=value&other=123");
        assertThat(result.industry()).isEqualTo("Research & Development");
    }

    @Test
    void toSummaryDto_ShouldHandleSpecialCharactersInFields() {
        // Given
        organization.setName("Summary Test & Co.");
        organization.setIndustry("Manufacturing & Assembly");

        // When
        OrganizationSummaryDto result = organizationMapper.toSummaryDto(organization);

        // Then
        assertThat(result.name()).isEqualTo("Summary Test & Co.");
        assertThat(result.industry()).isEqualTo("Manufacturing & Assembly");
    }

    @Test
    void toDto_ShouldHandleEmptyStringsAsValues() {
        // Given
        organization.setAddress("");
        organization.setPhone("");
        organization.setWebsite("");
        organization.setIndustry("");

        // When
        OrganizationDto result = organizationMapper.toDto(organization);

        // Then
        assertThat(result.address()).isEqualTo("");
        assertThat(result.phone()).isEqualTo("");
        assertThat(result.website()).isEqualTo("");
        assertThat(result.industry()).isEqualTo("");
    }

    @Test
    void toSummaryDto_ShouldHandleEmptyStringAsIndustry() {
        // Given
        organization.setIndustry("");

        // When
        OrganizationSummaryDto result = organizationMapper.toSummaryDto(organization);

        // Then
        assertThat(result.industry()).isEqualTo("");
    }
}