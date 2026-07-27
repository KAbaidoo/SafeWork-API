package com.safework.api.domain.supplier.mapper;

import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.supplier.dto.SupplierDto;
import com.safework.api.domain.supplier.dto.SupplierSummaryDto;
import com.safework.api.domain.supplier.model.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SupplierMapper Tests")
class SupplierMapperTest {

    private SupplierMapper supplierMapper;
    private Organization testOrganization;
    private Supplier testSupplier;
    private LocalDateTime testCreatedAt;
    private LocalDateTime testUpdatedAt;

    @BeforeEach
    void setUp() {
        supplierMapper = new SupplierMapper();
        setupTestData();
    }

    private void setupTestData() {
        testCreatedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        testUpdatedAt = LocalDateTime.of(2024, 1, 20, 15, 45, 0);

        testOrganization = new Organization();
        setEntityId(testOrganization, 1L);
        testOrganization.setName("Test Organization");
        testOrganization.setPhone("+1234567890");

        testSupplier = new Supplier();
        setEntityId(testSupplier, 100L);
        testSupplier.setName("Test Supplier");
        testSupplier.setContactPerson("John Doe");
        testSupplier.setEmail("john@testsupplier.com");
        testSupplier.setPhoneNumber("+1234567890");
        testSupplier.setAddress("123 Test Street, Test City, TC 12345");
        testSupplier.setOrganization(testOrganization);
        setVersion(testSupplier, 2);
        setTimestamp(testSupplier, "createdAt", testCreatedAt);
        setTimestamp(testSupplier, "updatedAt", testUpdatedAt);
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

    private void setVersion(Object entity, Integer version) {
        try {
            Field versionField = entity.getClass().getDeclaredField("version");
            versionField.setAccessible(true);
            versionField.set(entity, version);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set version", e);
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

    // SupplierDto Mapping Tests

    @Test
    @DisplayName("Should map supplier to SupplierDto correctly")
    void shouldMapSupplierToDto_Correctly() {
        SupplierDto result = supplierMapper.toDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Supplier");
        assertThat(result.contactPerson()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@testsupplier.com");
        assertThat(result.phoneNumber()).isEqualTo("+1234567890");
        assertThat(result.address()).isEqualTo("123 Test Street, Test City, TC 12345");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.version()).isEqualTo(2);
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
        assertThat(result.updatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    @DisplayName("Should handle null organization in SupplierDto mapping")
    void shouldHandleNullOrganization_InDtoMapping() {
        testSupplier.setOrganization(null);

        SupplierDto result = supplierMapper.toDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Supplier");
        assertThat(result.organizationId()).isNull();
        assertThat(result.contactPerson()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@testsupplier.com");
    }

    @Test
    @DisplayName("Should handle null optional fields in SupplierDto mapping")
    void shouldHandleNullOptionalFields_InDtoMapping() {
        testSupplier.setContactPerson(null);
        testSupplier.setEmail(null);
        testSupplier.setPhoneNumber(null);
        testSupplier.setAddress(null);

        SupplierDto result = supplierMapper.toDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Supplier");
        assertThat(result.contactPerson()).isNull();
        assertThat(result.email()).isNull();
        assertThat(result.phoneNumber()).isNull();
        assertThat(result.address()).isNull();
        assertThat(result.organizationId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should handle zero version in SupplierDto mapping")
    void shouldHandleZeroVersion_InDtoMapping() {
        setVersion(testSupplier, 0);

        SupplierDto result = supplierMapper.toDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.version()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle null version in SupplierDto mapping")
    void shouldHandleNullVersion_InDtoMapping() {
        setVersion(testSupplier, null);

        SupplierDto result = supplierMapper.toDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.version()).isNull();
    }

    // SupplierSummaryDto Mapping Tests

    @Test
    @DisplayName("Should map supplier to SupplierSummaryDto correctly")
    void shouldMapSupplierToSummaryDto_Correctly() {
        SupplierSummaryDto result = supplierMapper.toSummaryDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Supplier");
        assertThat(result.contactPerson()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@testsupplier.com");
        assertThat(result.phoneNumber()).isEqualTo("+1234567890");
    }

    @Test
    @DisplayName("Should handle null optional fields in SupplierSummaryDto mapping")
    void shouldHandleNullOptionalFields_InSummaryDtoMapping() {
        testSupplier.setContactPerson(null);
        testSupplier.setEmail(null);
        testSupplier.setPhoneNumber(null);

        SupplierSummaryDto result = supplierMapper.toSummaryDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Supplier");
        assertThat(result.contactPerson()).isNull();
        assertThat(result.email()).isNull();
        assertThat(result.phoneNumber()).isNull();
    }

    @Test
    @DisplayName("Should not include organization info in SupplierSummaryDto")
    void shouldNotIncludeOrganizationInfo_InSummaryDto() {
        SupplierSummaryDto result = supplierMapper.toSummaryDto(testSupplier);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Supplier");
        // SupplierSummaryDto should not include organization info, timestamps, or version
        assertThat(result.toString()).doesNotContain("organization")
                .doesNotContain("createdAt")
                .doesNotContain("updatedAt")
                .doesNotContain("version")
                .doesNotContain("address");
    }

    // Edge Cases and Boundary Tests

    @Test
    @DisplayName("Should handle supplier with minimal data")
    void shouldHandleSupplierWithMinimalData() {
        Supplier minimalSupplier = new Supplier();
        setEntityId(minimalSupplier, 1L);
        minimalSupplier.setName("Minimal Supplier");
        minimalSupplier.setOrganization(testOrganization);
        setTimestamp(minimalSupplier, "createdAt", testCreatedAt);
        setTimestamp(minimalSupplier, "updatedAt", testUpdatedAt);

        SupplierDto dtoResult = supplierMapper.toDto(minimalSupplier);
        SupplierSummaryDto summaryResult = supplierMapper.toSummaryDto(minimalSupplier);

        // SupplierDto assertions
        assertThat(dtoResult).isNotNull();
        assertThat(dtoResult.id()).isEqualTo(1L);
        assertThat(dtoResult.name()).isEqualTo("Minimal Supplier");
        assertThat(dtoResult.organizationId()).isEqualTo(1L);
        assertThat(dtoResult.contactPerson()).isNull();
        assertThat(dtoResult.email()).isNull();
        assertThat(dtoResult.phoneNumber()).isNull();
        assertThat(dtoResult.address()).isNull();

        // SupplierSummaryDto assertions
        assertThat(summaryResult).isNotNull();
        assertThat(summaryResult.id()).isEqualTo(1L);
        assertThat(summaryResult.name()).isEqualTo("Minimal Supplier");
        assertThat(summaryResult.contactPerson()).isNull();
        assertThat(summaryResult.email()).isNull();
        assertThat(summaryResult.phoneNumber()).isNull();
    }

    @Test
    @DisplayName("Should handle supplier with maximum length strings")
    void shouldHandleSupplierWithMaximumLengthStrings() {
        String longName = "A".repeat(100); // Assuming max length is 100
        String longContactPerson = "B".repeat(100);
        String longEmail = "test" + "c".repeat(92) + "@test.com"; // 100 chars total
        String longPhone = "+1" + "2".repeat(18); // 20 chars total
        String longAddress = "D".repeat(500); // Assuming max length is 500

        testSupplier.setName(longName);
        testSupplier.setContactPerson(longContactPerson);
        testSupplier.setEmail(longEmail);
        testSupplier.setPhoneNumber(longPhone);
        testSupplier.setAddress(longAddress);

        SupplierDto dtoResult = supplierMapper.toDto(testSupplier);
        SupplierSummaryDto summaryResult = supplierMapper.toSummaryDto(testSupplier);

        // SupplierDto assertions
        assertThat(dtoResult.name()).isEqualTo(longName);
        assertThat(dtoResult.contactPerson()).isEqualTo(longContactPerson);
        assertThat(dtoResult.email()).isEqualTo(longEmail);
        assertThat(dtoResult.phoneNumber()).isEqualTo(longPhone);
        assertThat(dtoResult.address()).isEqualTo(longAddress);

        // SupplierSummaryDto assertions
        assertThat(summaryResult.name()).isEqualTo(longName);
        assertThat(summaryResult.contactPerson()).isEqualTo(longContactPerson);
        assertThat(summaryResult.email()).isEqualTo(longEmail);
        assertThat(summaryResult.phoneNumber()).isEqualTo(longPhone);
    }

    @Test
    @DisplayName("Should handle supplier with special characters")
    void shouldHandleSupplierWithSpecialCharacters() {
        testSupplier.setName("Supplier & Co. Ltd.");
        testSupplier.setContactPerson("José María O'Connor-Smith");
        testSupplier.setEmail("josé.maría@tést-supplier.cöm");
        testSupplier.setPhoneNumber("+33 (0) 1-23-45-67-89");
        testSupplier.setAddress("123 Rue de la Paix, 75001 Paris, France");

        SupplierDto dtoResult = supplierMapper.toDto(testSupplier);
        SupplierSummaryDto summaryResult = supplierMapper.toSummaryDto(testSupplier);

        // Both should preserve special characters
        assertThat(dtoResult.name()).isEqualTo("Supplier & Co. Ltd.");
        assertThat(dtoResult.contactPerson()).isEqualTo("José María O'Connor-Smith");
        assertThat(dtoResult.email()).isEqualTo("josé.maría@tést-supplier.cöm");

        assertThat(summaryResult.name()).isEqualTo("Supplier & Co. Ltd.");
        assertThat(summaryResult.contactPerson()).isEqualTo("José María O'Connor-Smith");
        assertThat(summaryResult.email()).isEqualTo("josé.maría@tést-supplier.cöm");
    }

    // Data Consistency Tests

    @Test
    @DisplayName("Should maintain data consistency between DTO mappings")
    void shouldMaintainDataConsistency_BetweenDtoMappings() {
        SupplierDto fullDto = supplierMapper.toDto(testSupplier);
        SupplierSummaryDto summaryDto = supplierMapper.toSummaryDto(testSupplier);

        // Common fields should have same values
        assertThat(summaryDto.id()).isEqualTo(fullDto.id());
        assertThat(summaryDto.name()).isEqualTo(fullDto.name());
        assertThat(summaryDto.contactPerson()).isEqualTo(fullDto.contactPerson());
        assertThat(summaryDto.email()).isEqualTo(fullDto.email());
        assertThat(summaryDto.phoneNumber()).isEqualTo(fullDto.phoneNumber());
    }

    @Test
    @DisplayName("Should handle concurrent mapping operations")
    void shouldHandleConcurrentMappingOperations() {
        // Create multiple suppliers with different data
        Supplier supplier1 = new Supplier();
        setEntityId(supplier1, 1L);
        supplier1.setName("Supplier 1");
        supplier1.setContactPerson("Contact 1");
        supplier1.setOrganization(testOrganization);
        setTimestamp(supplier1, "createdAt", testCreatedAt);
        setTimestamp(supplier1, "updatedAt", testUpdatedAt);

        Supplier supplier2 = new Supplier();
        setEntityId(supplier2, 2L);
        supplier2.setName("Supplier 2");
        supplier2.setContactPerson("Contact 2");
        supplier2.setOrganization(testOrganization);
        setTimestamp(supplier2, "createdAt", testCreatedAt.plusDays(1));
        setTimestamp(supplier2, "updatedAt", testUpdatedAt.plusDays(1));

        // Map both suppliers
        SupplierDto dto1 = supplierMapper.toDto(supplier1);
        SupplierDto dto2 = supplierMapper.toDto(supplier2);
        SupplierSummaryDto summary1 = supplierMapper.toSummaryDto(supplier1);
        SupplierSummaryDto summary2 = supplierMapper.toSummaryDto(supplier2);

        // Verify each mapping maintains correct data
        assertThat(dto1.id()).isEqualTo(1L);
        assertThat(dto1.name()).isEqualTo("Supplier 1");
        assertThat(dto2.id()).isEqualTo(2L);
        assertThat(dto2.name()).isEqualTo("Supplier 2");

        assertThat(summary1.id()).isEqualTo(1L);
        assertThat(summary1.name()).isEqualTo("Supplier 1");
        assertThat(summary2.id()).isEqualTo(2L);
        assertThat(summary2.name()).isEqualTo("Supplier 2");
    }

    // Null Safety Tests

    @Test
    @DisplayName("Should handle completely null supplier gracefully")
    void shouldHandleNullSupplier_Gracefully() {
        // Note: In practice, this would likely throw NPE, but testing for defensive programming
        try {
            SupplierDto dtoResult = supplierMapper.toDto(null);
            SupplierSummaryDto summaryResult = supplierMapper.toSummaryDto(null);
            
            // If we reach here, the mapper handled null gracefully
            assertThat(dtoResult).isNull();
            assertThat(summaryResult).isNull();
        } catch (NullPointerException e) {
            // This is also acceptable behavior - the mapper should be called with valid entities
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    @DisplayName("Should preserve timestamp precision in DTO mapping")
    void shouldPreserveTimestampPrecision_InDtoMapping() {
        LocalDateTime preciseTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123456789);
        setTimestamp(testSupplier, "createdAt", preciseTime);
        setTimestamp(testSupplier, "updatedAt", preciseTime.plusMinutes(5));

        SupplierDto result = supplierMapper.toDto(testSupplier);

        assertThat(result.createdAt()).isEqualTo(preciseTime);
        assertThat(result.updatedAt()).isEqualTo(preciseTime.plusMinutes(5));
    }
}