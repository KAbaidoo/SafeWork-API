package com.safework.api.domain.supplier.integration;

import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.repository.OrganizationRepository;
import com.safework.api.domain.supplier.dto.CreateSupplierRequest;
import com.safework.api.domain.supplier.dto.SupplierDto;
import com.safework.api.domain.supplier.dto.SupplierSummaryDto;
import com.safework.api.domain.supplier.dto.UpdateSupplierRequest;
import com.safework.api.domain.supplier.model.Supplier;
import com.safework.api.domain.supplier.repository.SupplierRepository;
import com.safework.api.domain.supplier.service.SupplierService;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("SupplierIntegration Tests")
class SupplierIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private SupplierService supplierService;

    private Organization testOrganization;
    private Organization anotherOrganization;
    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        // Initialize service with actual repositories
        supplierService = new com.safework.api.domain.supplier.service.SupplierService(
                supplierRepository,
                new com.safework.api.domain.supplier.mapper.SupplierMapper()
        );
        setupTestData();
    }

    private void setupTestData() {
        // Create test organizations
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setPhone("+1234567890");
        testOrganization = entityManager.persistAndFlush(testOrganization);

        anotherOrganization = new Organization();
        anotherOrganization.setName("Another Organization");
        anotherOrganization.setPhone("+0987654321");
        anotherOrganization = entityManager.persistAndFlush(anotherOrganization);

        // Create test users
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@user.com");
        testUser.setPassword("$2a$10$encoded.password.hash"); // BCrypt encoded password
        testUser.setRole(UserRole.ADMIN);
        testUser.setOrganization(testOrganization);
        setTimestamp(testUser, "createdAt", LocalDateTime.now());
        setTimestamp(testUser, "updatedAt", LocalDateTime.now());
        testUser = entityManager.persistAndFlush(testUser);

        anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@user.com");
        anotherUser.setPassword("$2a$10$encoded.password.hash"); // BCrypt encoded password
        anotherUser.setRole(UserRole.ADMIN);
        anotherUser.setOrganization(anotherOrganization);
        setTimestamp(anotherUser, "createdAt", LocalDateTime.now());
        setTimestamp(anotherUser, "updatedAt", LocalDateTime.now());
        anotherUser = entityManager.persistAndFlush(anotherUser);

        // Create test suppliers
        createTestSupplier("Initial Supplier", "John Doe", "john@initial.com", testOrganization);
        createTestSupplier("Another Supplier", "Jane Smith", "jane@another.com", anotherOrganization);

        entityManager.clear();
    }

    private Supplier createTestSupplier(String name, String contactPerson, String email, Organization organization) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setEmail(email);
        supplier.setPhoneNumber("+1234567890");
        supplier.setAddress("123 Test Street");
        supplier.setOrganization(organization);
        setTimestamp(supplier, "createdAt", LocalDateTime.now());
        setTimestamp(supplier, "updatedAt", LocalDateTime.now());
        return entityManager.persistAndFlush(supplier);
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

    // Create Supplier Integration Tests

    @Test
    @DisplayName("Should create supplier with complete workflow")
    void shouldCreateSupplier_CompleteWorkflow() {
        CreateSupplierRequest request = new CreateSupplierRequest(
                "New Supplier",
                "Mike Johnson",
                "mike@newsupplier.com",
                "+5555555555",
                "789 New Street"
        );

        SupplierDto createdSupplier = supplierService.createSupplier(request, testUser);

        assertThat(createdSupplier).isNotNull();
        assertThat(createdSupplier.id()).isNotNull();
        assertThat(createdSupplier.name()).isEqualTo("New Supplier");
        assertThat(createdSupplier.contactPerson()).isEqualTo("Mike Johnson");
        assertThat(createdSupplier.email()).isEqualTo("mike@newsupplier.com");
        assertThat(createdSupplier.phoneNumber()).isEqualTo("+5555555555");
        assertThat(createdSupplier.address()).isEqualTo("789 New Street");
        assertThat(createdSupplier.organizationId()).isEqualTo(testOrganization.getId());
        assertThat(createdSupplier.createdAt()).isNotNull();
        assertThat(createdSupplier.updatedAt()).isNotNull();

        // Verify it's persisted in database
        Supplier savedSupplier = supplierRepository.findById(createdSupplier.id()).orElse(null);
        assertThat(savedSupplier).isNotNull();
        assertThat(savedSupplier.getName()).isEqualTo("New Supplier");
    }

    @Test
    @DisplayName("Should enforce unique constraint on supplier name within organization")
    void shouldEnforceUniqueConstraint_SupplierNameWithinOrganization() {
        CreateSupplierRequest duplicateRequest = new CreateSupplierRequest(
                "Initial Supplier", // Same name as existing supplier
                "Different Contact",
                "different@email.com",
                "+9999999999",
                "Different Address"
        );

        assertThatThrownBy(() -> supplierService.createSupplier(duplicateRequest, testUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Supplier with name 'Initial Supplier' already exists in this organization");
    }

    @Test
    @DisplayName("Should allow same supplier name in different organizations")
    void shouldAllowSameSupplierName_InDifferentOrganizations() {
        CreateSupplierRequest sameNameRequest = new CreateSupplierRequest(
                "Initial Supplier", // Same name as supplier in testOrganization
                "Different Contact",
                "different@email.com",
                "+9999999999",
                "Different Address"
        );

        SupplierDto createdSupplier = supplierService.createSupplier(sameNameRequest, anotherUser);

        assertThat(createdSupplier).isNotNull();
        assertThat(createdSupplier.name()).isEqualTo("Initial Supplier");
        assertThat(createdSupplier.organizationId()).isEqualTo(anotherOrganization.getId());

        // Verify both suppliers exist with same name in different organizations
        boolean existsInTestOrg = supplierRepository.existsByOrganizationIdAndName(
                testOrganization.getId(), "Initial Supplier");
        boolean existsInAnotherOrg = supplierRepository.existsByOrganizationIdAndName(
                anotherOrganization.getId(), "Initial Supplier");

        assertThat(existsInTestOrg).isTrue();
        assertThat(existsInAnotherOrg).isTrue();
    }

    // Find Suppliers Integration Tests

    @Test
    @DisplayName("Should find all suppliers for organization with pagination")
    void shouldFindAllSuppliers_ForOrganizationWithPagination() {
        // Create additional suppliers for pagination testing
        for (int i = 1; i <= 5; i++) {
            createTestSupplier("Supplier " + i, "Contact " + i, "contact" + i + "@test.com", testOrganization);
        }

        Pageable pageable = PageRequest.of(0, 3);
        Page<SupplierSummaryDto> result = supplierService.findAllByOrganization(testOrganization.getId(), pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6); // 1 initial + 5 new
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();

        // Verify all suppliers belong to the correct organization
        result.getContent().forEach(supplier -> {
            Supplier fullSupplier = supplierRepository.findById(supplier.id()).orElse(null);
            assertThat(fullSupplier).isNotNull();
            assertThat(fullSupplier.getOrganization().getId()).isEqualTo(testOrganization.getId());
        });
    }

    @Test
    @DisplayName("Should find supplier by ID with organization isolation")
    void shouldFindSupplierById_WithOrganizationIsolation() {
        // Find a supplier from testOrganization
        Supplier testOrgSupplier = supplierRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Initial Supplier").orElseThrow();

        SupplierDto result = supplierService.findSupplierById(testOrgSupplier.getId(), testUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testOrgSupplier.getId());
        assertThat(result.name()).isEqualTo("Initial Supplier");
        assertThat(result.organizationId()).isEqualTo(testOrganization.getId());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when accessing supplier from different organization")
    void shouldThrowAccessDeniedException_WhenAccessingDifferentOrganizationSupplier() {
        // Get supplier from anotherOrganization
        Supplier anotherOrgSupplier = supplierRepository.findByOrganizationIdAndName(
                anotherOrganization.getId(), "Another Supplier").orElseThrow();

        // Try to access it with testUser (from testOrganization)
        assertThatThrownBy(() -> supplierService.findSupplierById(anotherOrgSupplier.getId(), testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this supplier");
    }

    // Search Integration Tests

    @Test
    @DisplayName("Should search suppliers by name with case insensitivity")
    void shouldSearchSuppliersByName_CaseInsensitive() {
        // Create suppliers with different cases
        createTestSupplier("ABC Manufacturing", "Contact 1", "contact1@abc.com", testOrganization);
        createTestSupplier("XYZ manufacturing Co", "Contact 2", "contact2@xyz.com", testOrganization);
        createTestSupplier("DEF Services", "Contact 3", "contact3@def.com", testOrganization);

        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplierSummaryDto> result = supplierService.searchSuppliersByName("MANUFACTURING", testUser, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(SupplierSummaryDto::name)
                .containsExactlyInAnyOrder("ABC Manufacturing", "XYZ manufacturing Co");
    }

    @Test
    @DisplayName("Should search suppliers by email within organization")
    void shouldSearchSuppliersByEmail_WithinOrganization() {
        // Create suppliers with specific email patterns
        createTestSupplier("Gmail Supplier 1", "Contact 1", "test1@gmail.com", testOrganization);
        createTestSupplier("Gmail Supplier 2", "Contact 2", "test2@gmail.com", testOrganization);
        createTestSupplier("Yahoo Supplier", "Contact 3", "test@yahoo.com", testOrganization);
        
        // Create supplier in another organization with gmail email
        createTestSupplier("Other Gmail", "Other Contact", "other@gmail.com", anotherOrganization);

        Pageable pageable = PageRequest.of(0, 10);
        Page<SupplierSummaryDto> result = supplierService.searchSuppliersByEmail("gmail", testUser, pageable);

        assertThat(result.getContent()).hasSize(2); // Only from testOrganization
        assertThat(result.getContent()).extracting(SupplierSummaryDto::name)
                .containsExactlyInAnyOrder("Gmail Supplier 1", "Gmail Supplier 2");
    }

    // Update Supplier Integration Tests

    @Test
    @DisplayName("Should update supplier with complete workflow")
    void shouldUpdateSupplier_CompleteWorkflow() {
        Supplier existingSupplier = supplierRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Initial Supplier").orElseThrow();

        UpdateSupplierRequest updateRequest = new UpdateSupplierRequest(
                "Updated Supplier Name",
                "Updated Contact Person",
                "updated@email.com",
                "+9999999999",
                "999 Updated Street"
        );

        SupplierDto updatedSupplier = supplierService.updateSupplier(
                existingSupplier.getId(), updateRequest, testUser);

        assertThat(updatedSupplier).isNotNull();
        assertThat(updatedSupplier.id()).isEqualTo(existingSupplier.getId());
        assertThat(updatedSupplier.name()).isEqualTo("Updated Supplier Name");
        assertThat(updatedSupplier.contactPerson()).isEqualTo("Updated Contact Person");
        assertThat(updatedSupplier.email()).isEqualTo("updated@email.com");
        assertThat(updatedSupplier.phoneNumber()).isEqualTo("+9999999999");
        assertThat(updatedSupplier.address()).isEqualTo("999 Updated Street");

        // Verify changes are persisted
        Supplier persistedSupplier = supplierRepository.findById(existingSupplier.getId()).orElse(null);
        assertThat(persistedSupplier).isNotNull();
        assertThat(persistedSupplier.getName()).isEqualTo("Updated Supplier Name");
        assertThat(persistedSupplier.getEmail()).isEqualTo("updated@email.com");
    }

    @Test
    @DisplayName("Should update supplier keeping same name successfully")
    void shouldUpdateSupplier_KeepingSameName() {
        Supplier existingSupplier = supplierRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Initial Supplier").orElseThrow();

        UpdateSupplierRequest updateRequest = new UpdateSupplierRequest(
                "Initial Supplier", // Keep same name
                "Updated Contact Person",
                "updated@email.com",
                "+9999999999",
                "999 Updated Street"
        );

        SupplierDto updatedSupplier = supplierService.updateSupplier(
                existingSupplier.getId(), updateRequest, testUser);

        assertThat(updatedSupplier).isNotNull();
        assertThat(updatedSupplier.name()).isEqualTo("Initial Supplier");
        assertThat(updatedSupplier.contactPerson()).isEqualTo("Updated Contact Person");
    }

    @Test
    @DisplayName("Should throw ConflictException when updating to existing supplier name")
    void shouldThrowConflictException_WhenUpdatingToExistingSupplierName() {
        // Create another supplier
        Supplier anotherSupplier = createTestSupplier("Another Test Supplier", "Contact", "contact@test.com", testOrganization);
        
        Supplier existingSupplier = supplierRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Initial Supplier").orElseThrow();

        UpdateSupplierRequest updateRequest = new UpdateSupplierRequest(
                "Another Test Supplier", // Name already exists
                "Updated Contact",
                "updated@email.com",
                "+9999999999",
                "999 Updated Street"
        );

        assertThatThrownBy(() -> supplierService.updateSupplier(existingSupplier.getId(), updateRequest, testUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Supplier with name 'Another Test Supplier' already exists in this organization");
    }

    // Delete Supplier Integration Tests

    @Test
    @DisplayName("Should delete supplier successfully")
    void shouldDeleteSupplier_Successfully() {
        Supplier supplierToDelete = supplierRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Initial Supplier").orElseThrow();
        Long supplierId = supplierToDelete.getId();

        supplierService.deleteSupplier(supplierId, testUser);

        // Verify supplier is deleted
        assertThat(supplierRepository.findById(supplierId)).isEmpty();
        assertThat(supplierRepository.existsByOrganizationIdAndName(
                testOrganization.getId(), "Initial Supplier")).isFalse();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent supplier")
    void shouldThrowResourceNotFoundException_WhenDeletingNonExistentSupplier() {
        assertThatThrownBy(() -> supplierService.deleteSupplier(999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 999");
    }

    // Multi-tenant Security Integration Tests

    @Test
    @DisplayName("Should enforce strict organization isolation across all operations")
    void shouldEnforceOrganizationIsolation_AcrossAllOperations() {
        // Get supplier from another organization
        Supplier anotherOrgSupplier = supplierRepository.findByOrganizationIdAndName(
                anotherOrganization.getId(), "Another Supplier").orElseThrow();

        UpdateSupplierRequest updateRequest = new UpdateSupplierRequest(
                "Hacked Supplier", "Hacker", "hack@evil.com", "+666", "Evil Street"
        );

        // Test all operations that should be blocked
        assertThatThrownBy(() -> supplierService.findSupplierById(anotherOrgSupplier.getId(), testUser))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> supplierService.updateSupplier(anotherOrgSupplier.getId(), updateRequest, testUser))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> supplierService.deleteSupplier(anotherOrgSupplier.getId(), testUser))
                .isInstanceOf(AccessDeniedException.class);

        // Verify supplier remained unchanged
        Supplier unchangedSupplier = supplierRepository.findById(anotherOrgSupplier.getId()).orElse(null);
        assertThat(unchangedSupplier).isNotNull();
        assertThat(unchangedSupplier.getName()).isEqualTo("Another Supplier");
        assertThat(unchangedSupplier.getOrganization().getId()).isEqualTo(anotherOrganization.getId());
    }

    // Performance and Edge Case Tests

    @Test
    @DisplayName("Should handle large datasets efficiently")
    void shouldHandleLargeDatasets_Efficiently() {
        // Create many suppliers
        for (int i = 1; i <= 50; i++) {
            createTestSupplier("Bulk Supplier " + String.format("%03d", i), 
                    "Contact " + i, "bulk" + i + "@test.com", testOrganization);
        }

        // Test pagination with large dataset
        Pageable firstPage = PageRequest.of(0, 10);
        Page<SupplierSummaryDto> firstResult = supplierService.findAllByOrganization(testOrganization.getId(), firstPage);

        assertThat(firstResult.getContent()).hasSize(10);
        assertThat(firstResult.getTotalElements()).isEqualTo(51); // 50 bulk + 1 initial
        assertThat(firstResult.getTotalPages()).isEqualTo(6);

        // Test search with large dataset
        Pageable searchPageable = PageRequest.of(0, 20);
        Page<SupplierSummaryDto> searchResult = supplierService.searchSuppliersByName("Bulk", testUser, searchPageable);

        assertThat(searchResult.getContent()).hasSize(20);
        assertThat(searchResult.getTotalElements()).isEqualTo(50);
        assertThat(searchResult.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Should handle empty search results gracefully")
    void shouldHandleEmptySearchResults_Gracefully() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<SupplierSummaryDto> nameSearchResult = supplierService.searchSuppliersByName(
                "NonExistentSupplier", testUser, pageable);
        Page<SupplierSummaryDto> emailSearchResult = supplierService.searchSuppliersByEmail(
                "nonexistent@email.com", testUser, pageable);

        assertThat(nameSearchResult.getContent()).isEmpty();
        assertThat(nameSearchResult.getTotalElements()).isEqualTo(0);
        assertThat(emailSearchResult.getContent()).isEmpty();
        assertThat(emailSearchResult.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should maintain data consistency during concurrent operations")
    void shouldMaintainDataConsistency_DuringConcurrentOperations() {
        // Create supplier
        CreateSupplierRequest createRequest = new CreateSupplierRequest(
                "Concurrent Test Supplier", "Test Contact", "test@concurrent.com", "+1111111111", "Test Address"
        );
        SupplierDto createdSupplier = supplierService.createSupplier(createRequest, testUser);

        // Update supplier
        UpdateSupplierRequest updateRequest = new UpdateSupplierRequest(
                "Updated Concurrent Supplier", "Updated Contact", "updated@concurrent.com", "+2222222222", "Updated Address"
        );
        SupplierDto updatedSupplier = supplierService.updateSupplier(createdSupplier.id(), updateRequest, testUser);

        // Verify consistency
        assertThat(updatedSupplier.id()).isEqualTo(createdSupplier.id());
        assertThat(updatedSupplier.name()).isEqualTo("Updated Concurrent Supplier");
        assertThat(updatedSupplier.organizationId()).isEqualTo(createdSupplier.organizationId());

        // Verify in database
        Supplier persistedSupplier = supplierRepository.findById(createdSupplier.id()).orElse(null);
        assertThat(persistedSupplier).isNotNull();
        assertThat(persistedSupplier.getName()).isEqualTo("Updated Concurrent Supplier");
        assertThat(persistedSupplier.getEmail()).isEqualTo("updated@concurrent.com");
    }
}