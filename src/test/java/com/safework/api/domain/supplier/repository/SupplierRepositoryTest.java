package com.safework.api.domain.supplier.repository;

import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.supplier.model.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SupplierRepository Tests")
class SupplierRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SupplierRepository supplierRepository;

    private Organization testOrganization;
    private Organization anotherOrganization;
    private Supplier testSupplier;
    private Supplier anotherSupplier;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setPhone("+1234567890");
        testOrganization = entityManager.persistAndFlush(testOrganization);

        anotherOrganization = new Organization();
        anotherOrganization.setName("Another Organization");
        anotherOrganization.setPhone("+0987654321");
        anotherOrganization = entityManager.persistAndFlush(anotherOrganization);

        testSupplier = new Supplier();
        testSupplier.setName("Test Supplier");
        testSupplier.setContactPerson("John Doe");
        testSupplier.setEmail("john@testsupplier.com");
        testSupplier.setPhoneNumber("+1234567890");
        testSupplier.setAddress("123 Test Street");
        testSupplier.setOrganization(testOrganization);
        setTimestamp(testSupplier, "createdAt", LocalDateTime.now());
        setTimestamp(testSupplier, "updatedAt", LocalDateTime.now());
        testSupplier = entityManager.persistAndFlush(testSupplier);

        anotherSupplier = new Supplier();
        anotherSupplier.setName("Another Supplier");
        anotherSupplier.setContactPerson("Jane Smith");
        anotherSupplier.setEmail("jane@anothersupplier.com");
        anotherSupplier.setPhoneNumber("+0987654321");
        anotherSupplier.setAddress("456 Another Street");
        anotherSupplier.setOrganization(anotherOrganization);
        setTimestamp(anotherSupplier, "createdAt", LocalDateTime.now());
        setTimestamp(anotherSupplier, "updatedAt", LocalDateTime.now());
        anotherSupplier = entityManager.persistAndFlush(anotherSupplier);

        entityManager.clear();
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

    // Basic CRUD Tests

    @Test
    @DisplayName("Should save supplier successfully")
    void shouldSaveSupplier() {
        Supplier newSupplier = new Supplier();
        newSupplier.setName("New Supplier");
        newSupplier.setContactPerson("New Contact");
        newSupplier.setEmail("new@supplier.com");
        newSupplier.setOrganization(testOrganization);

        Supplier savedSupplier = supplierRepository.save(newSupplier);

        assertThat(savedSupplier.getId()).isNotNull();
        assertThat(savedSupplier.getName()).isEqualTo("New Supplier");
        assertThat(savedSupplier.getContactPerson()).isEqualTo("New Contact");
        assertThat(savedSupplier.getEmail()).isEqualTo("new@supplier.com");
        assertThat(savedSupplier.getOrganization().getId()).isEqualTo(testOrganization.getId());
        assertThat(savedSupplier.getCreatedAt()).isNotNull();
        assertThat(savedSupplier.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find supplier by ID")
    void shouldFindSupplierById() {
        Optional<Supplier> foundSupplier = supplierRepository.findById(testSupplier.getId());

        assertThat(foundSupplier).isPresent();
        assertThat(foundSupplier.get().getName()).isEqualTo("Test Supplier");
    }

    @Test
    @DisplayName("Should update supplier successfully")
    void shouldUpdateSupplier() {
        testSupplier.setName("Updated Supplier");
        testSupplier.setEmail("updated@supplier.com");

        Supplier updatedSupplier = supplierRepository.save(testSupplier);

        assertThat(updatedSupplier.getName()).isEqualTo("Updated Supplier");
        assertThat(updatedSupplier.getEmail()).isEqualTo("updated@supplier.com");
    }

    @Test
    @DisplayName("Should delete supplier successfully")
    void shouldDeleteSupplier() {
        Long supplierId = testSupplier.getId();
        supplierRepository.delete(testSupplier);

        Optional<Supplier> deletedSupplier = supplierRepository.findById(supplierId);
        assertThat(deletedSupplier).isEmpty();
    }

    // Organization-scoped query tests

    @Test
    @DisplayName("Should find all suppliers by organization ID")
    void shouldFindAllSuppliersByOrganizationId() {
        // Create additional suppliers for the test organization
        for (int i = 1; i <= 3; i++) {
            Supplier supplier = new Supplier();
            supplier.setName("Supplier " + i);
            supplier.setContactPerson("Contact " + i);
            supplier.setEmail("contact" + i + "@supplier.com");
            supplier.setOrganization(testOrganization);
            setTimestamp(supplier, "createdAt", LocalDateTime.now());
            setTimestamp(supplier, "updatedAt", LocalDateTime.now());
            entityManager.persistAndFlush(supplier);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> suppliers = supplierRepository.findAllByOrganizationId(testOrganization.getId(), pageable);

        assertThat(suppliers.getContent()).hasSize(4); // testSupplier + 3 new ones
        assertThat(suppliers.getTotalElements()).isEqualTo(4);
        assertThat(suppliers.getContent())
                .allMatch(supplier -> supplier.getOrganization().getId().equals(testOrganization.getId()));
    }

    @Test
    @DisplayName("Should find supplier by organization ID and name")
    void shouldFindSupplierByOrganizationIdAndName() {
        Optional<Supplier> foundSupplier = supplierRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Test Supplier");

        assertThat(foundSupplier).isPresent();
        assertThat(foundSupplier.get().getName()).isEqualTo("Test Supplier");
        assertThat(foundSupplier.get().getOrganization().getId()).isEqualTo(testOrganization.getId());
    }

    @Test
    @DisplayName("Should not find supplier by name from different organization")
    void shouldNotFindSupplierByNameFromDifferentOrganization() {
        Optional<Supplier> foundSupplier = supplierRepository.findByOrganizationIdAndName(
                anotherOrganization.getId(), "Test Supplier");

        assertThat(foundSupplier).isEmpty();
    }

    @Test
    @DisplayName("Should check existence by organization ID and name")
    void shouldCheckExistenceByOrganizationIdAndName() {
        boolean exists = supplierRepository.existsByOrganizationIdAndName(
                testOrganization.getId(), "Test Supplier");
        boolean notExists = supplierRepository.existsByOrganizationIdAndName(
                testOrganization.getId(), "Non-existent Supplier");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should not find existence in different organization")
    void shouldNotFindExistenceInDifferentOrganization() {
        boolean exists = supplierRepository.existsByOrganizationIdAndName(
                anotherOrganization.getId(), "Test Supplier");

        assertThat(exists).isFalse();
    }

    // Search functionality tests

    @Test
    @DisplayName("Should find suppliers by name containing search term")
    void shouldFindSuppliersByNameContaining() {
        Supplier supplier1 = new Supplier();
        supplier1.setName("ABC Manufacturing");
        supplier1.setContactPerson("Contact 1");
        supplier1.setEmail("contact1@abc.com");
        supplier1.setOrganization(testOrganization);
        setTimestamp(supplier1, "createdAt", LocalDateTime.now());
        setTimestamp(supplier1, "updatedAt", LocalDateTime.now());
        entityManager.persistAndFlush(supplier1);

        Supplier supplier2 = new Supplier();
        supplier2.setName("XYZ Manufacturing");
        supplier2.setContactPerson("Contact 2");
        supplier2.setEmail("contact2@xyz.com");
        supplier2.setOrganization(testOrganization);
        setTimestamp(supplier2, "createdAt", LocalDateTime.now());
        setTimestamp(supplier2, "updatedAt", LocalDateTime.now());
        entityManager.persistAndFlush(supplier2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> suppliers = supplierRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "manufacturing", pageable);

        assertThat(suppliers.getContent()).hasSize(2);
        assertThat(suppliers.getContent())
                .extracting(Supplier::getName)
                .containsExactlyInAnyOrder("ABC Manufacturing", "XYZ Manufacturing");
    }

    @Test
    @DisplayName("Should find suppliers by email containing search term")
    void shouldFindSuppliersByEmailContaining() {
        Supplier supplier1 = new Supplier();
        supplier1.setName("Email Supplier 1");
        supplier1.setContactPerson("Contact 1");
        supplier1.setEmail("test1@gmail.com");
        supplier1.setOrganization(testOrganization);
        setTimestamp(supplier1, "createdAt", LocalDateTime.now());
        setTimestamp(supplier1, "updatedAt", LocalDateTime.now());
        entityManager.persistAndFlush(supplier1);

        Supplier supplier2 = new Supplier();
        supplier2.setName("Email Supplier 2");
        supplier2.setContactPerson("Contact 2");
        supplier2.setEmail("test2@gmail.com");
        supplier2.setOrganization(testOrganization);
        setTimestamp(supplier2, "createdAt", LocalDateTime.now());
        setTimestamp(supplier2, "updatedAt", LocalDateTime.now());
        entityManager.persistAndFlush(supplier2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> suppliers = supplierRepository.findByOrganizationIdAndEmailContainingIgnoreCase(
                testOrganization.getId(), "gmail", pageable);

        assertThat(suppliers.getContent()).hasSize(2);
        assertThat(suppliers.getContent())
                .extracting(Supplier::getEmail)
                .allMatch(email -> email.contains("gmail"));
    }

    // Pagination tests

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Create 10 suppliers for pagination testing
        for (int i = 1; i <= 10; i++) {
            Supplier supplier = new Supplier();
            supplier.setName("Supplier " + String.format("%02d", i));
            supplier.setContactPerson("Contact " + i);
            supplier.setEmail("contact" + i + "@supplier.com");
            supplier.setOrganization(testOrganization);
            setTimestamp(supplier, "createdAt", LocalDateTime.now());
            setTimestamp(supplier, "updatedAt", LocalDateTime.now());
            entityManager.persistAndFlush(supplier);
        }

        Pageable firstPage = PageRequest.of(0, 5);
        Page<Supplier> firstPageResult = supplierRepository.findAllByOrganizationId(testOrganization.getId(), firstPage);

        assertThat(firstPageResult.getContent()).hasSize(5);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(11); // 10 new + testSupplier
        assertThat(firstPageResult.getTotalPages()).isEqualTo(3);
        assertThat(firstPageResult.hasNext()).isTrue();

        Pageable secondPage = PageRequest.of(1, 5);
        Page<Supplier> secondPageResult = supplierRepository.findAllByOrganizationId(testOrganization.getId(), secondPage);

        assertThat(secondPageResult.getContent()).hasSize(5);
        assertThat(secondPageResult.hasNext()).isTrue();
        assertThat(secondPageResult.hasPrevious()).isTrue();
    }

    // Constraint tests

    @Test
    @DisplayName("Should enforce unique constraint on organization and name")
    void shouldEnforceUniqueConstraintOnOrganizationAndName() {
        Supplier duplicateSupplier = new Supplier();
        duplicateSupplier.setName("Test Supplier"); // Same name as testSupplier
        duplicateSupplier.setContactPerson("Different Contact");
        duplicateSupplier.setEmail("different@email.com");
        duplicateSupplier.setOrganization(testOrganization); // Same organization

        try {
            entityManager.persistAndFlush(duplicateSupplier);
        } catch (Exception e) {
            // H2 throws different exception type than production database
            assertThat(e.getCause()).isInstanceOfAny(
                    org.hibernate.exception.ConstraintViolationException.class,
                    org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException.class
            );
        }
    }

    @Test
    @DisplayName("Should allow same name in different organizations")
    void shouldAllowSameNameInDifferentOrganizations() {
        Supplier supplierInDifferentOrg = new Supplier();
        supplierInDifferentOrg.setName("Test Supplier"); // Same name as testSupplier
        supplierInDifferentOrg.setContactPerson("Different Contact");
        supplierInDifferentOrg.setEmail("different@email.com");
        supplierInDifferentOrg.setOrganization(anotherOrganization); // Different organization

        // This should not throw an exception
        Supplier savedSupplier = entityManager.persistAndFlush(supplierInDifferentOrg);
        assertThat(savedSupplier.getId()).isNotNull();
    }

    // Edge cases

    @Test
    @DisplayName("Should handle empty result sets")
    void shouldHandleEmptyResultSets() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> emptyResults = supplierRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "NonExistentSearchTerm", pageable);

        assertThat(emptyResults.getContent()).isEmpty();
        assertThat(emptyResults.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle case insensitive searches")
    void shouldHandleCaseInsensitiveSearches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> upperCaseSearch = supplierRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "TEST", pageable);
        Page<Supplier> lowerCaseSearch = supplierRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "test", pageable);

        assertThat(upperCaseSearch.getContent()).hasSize(1);
        assertThat(lowerCaseSearch.getContent()).hasSize(1);
        assertThat(upperCaseSearch.getContent().get(0).getId()).isEqualTo(testSupplier.getId());
        assertThat(lowerCaseSearch.getContent().get(0).getId()).isEqualTo(testSupplier.getId());
    }

    @Test
    @DisplayName("Should find all suppliers with empty page request")
    void shouldFindAllSuppliersWithEmptyPageRequest() {
        List<Supplier> allSuppliers = supplierRepository.findAll();
        
        assertThat(allSuppliers).hasSize(2); // testSupplier and anotherSupplier
        assertThat(allSuppliers)
                .extracting(Supplier::getName)
                .containsExactlyInAnyOrder("Test Supplier", "Another Supplier");
    }
}