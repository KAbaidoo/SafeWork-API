package com.safework.api.domain.supplier.service;

import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.supplier.dto.CreateSupplierRequest;
import com.safework.api.domain.supplier.dto.SupplierDto;
import com.safework.api.domain.supplier.dto.SupplierSummaryDto;
import com.safework.api.domain.supplier.dto.UpdateSupplierRequest;
import com.safework.api.domain.supplier.mapper.SupplierMapper;
import com.safework.api.domain.supplier.model.Supplier;
import com.safework.api.domain.supplier.repository.SupplierRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierService Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private User testUser;
    private Organization testOrganization;
    private Organization anotherOrganization;
    private Supplier testSupplier;
    private CreateSupplierRequest createRequest;
    private UpdateSupplierRequest updateRequest;
    private SupplierDto supplierDto;
    private SupplierSummaryDto supplierSummaryDto;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testOrganization = new Organization();
        setEntityId(testOrganization, 1L);
        testOrganization.setName("Test Organization");

        anotherOrganization = new Organization();
        setEntityId(anotherOrganization, 2L);
        anotherOrganization.setName("Another Organization");

        testUser = new User();
        setEntityId(testUser, 1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.ADMIN);
        testUser.setOrganization(testOrganization);

        testSupplier = new Supplier();
        setEntityId(testSupplier, 1L);
        testSupplier.setName("Test Supplier");
        testSupplier.setContactPerson("John Doe");
        testSupplier.setEmail("john@testsupplier.com");
        testSupplier.setPhoneNumber("+1234567890");
        testSupplier.setAddress("123 Test Street");
        testSupplier.setOrganization(testOrganization);
        setTimestamp(testSupplier, "createdAt", LocalDateTime.now());
        setTimestamp(testSupplier, "updatedAt", LocalDateTime.now());

        createRequest = new CreateSupplierRequest(
                "New Supplier",
                "Jane Smith",
                "jane@newsupplier.com",
                "+0987654321",
                "456 New Street"
        );

        updateRequest = new UpdateSupplierRequest(
                "Updated Supplier",
                "Updated Contact",
                "updated@supplier.com",
                "+1111111111",
                "789 Updated Street"
        );

        supplierDto = new SupplierDto(
                1L,
                "Test Supplier",
                "John Doe",
                "john@testsupplier.com",
                "+1234567890",
                "123 Test Street",
                1L,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        supplierSummaryDto = new SupplierSummaryDto(
                1L,
                "Test Supplier",
                "John Doe",
                "john@testsupplier.com",
                "+1234567890"
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

    // Create Supplier Tests

    @Test
    @DisplayName("Should create supplier successfully")
    void shouldCreateSupplier_WhenValidRequest() {
        when(supplierRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDto);

        SupplierDto result = supplierService.createSupplier(createRequest, testUser);

        assertThat(result).isEqualTo(supplierDto);
        verify(supplierRepository).existsByOrganizationIdAndName(testOrganization.getId(), createRequest.name());
        verify(supplierRepository).save(any(Supplier.class));
        verify(supplierMapper).toDto(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when supplier name already exists")
    void shouldThrowConflictException_WhenSupplierNameExists() {
        when(supplierRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> supplierService.createSupplier(createRequest, testUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Supplier with name 'New Supplier' already exists in this organization");

        verify(supplierRepository).existsByOrganizationIdAndName(testOrganization.getId(), createRequest.name());
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    // Find All Suppliers Tests

    @Test
    @DisplayName("Should find all suppliers by organization")
    void shouldFindAllSuppliersByOrganization() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> supplierPage = new PageImpl<>(List.of(testSupplier));

        when(supplierRepository.findAllByOrganizationId(anyLong(), any(Pageable.class))).thenReturn(supplierPage);
        when(supplierMapper.toSummaryDto(any(Supplier.class))).thenReturn(supplierSummaryDto);

        Page<SupplierSummaryDto> result = supplierService.findAllByOrganization(testOrganization.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(supplierSummaryDto);
        verify(supplierRepository).findAllByOrganizationId(testOrganization.getId(), pageable);
        verify(supplierMapper).toSummaryDto(testSupplier);
    }

    // Find Supplier By ID Tests

    @Test
    @DisplayName("Should find supplier by ID successfully")
    void shouldFindSupplierById_WhenSupplierExists() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(testSupplier));
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDto);

        SupplierDto result = supplierService.findSupplierById(1L, testUser);

        assertThat(result).isEqualTo(supplierDto);
        verify(supplierRepository).findById(1L);
        verify(supplierMapper).toDto(testSupplier);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when supplier not found")
    void shouldThrowResourceNotFoundException_WhenSupplierNotFound() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.findSupplierById(999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 999");

        verify(supplierRepository).findById(999L);
        verify(supplierMapper, never()).toDto(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user accesses supplier from different organization")
    void shouldThrowAccessDeniedException_WhenUserAccessesDifferentOrganizationSupplier() {
        Supplier supplierFromAnotherOrg = new Supplier();
        setEntityId(supplierFromAnotherOrg, 2L);
        supplierFromAnotherOrg.setOrganization(anotherOrganization);

        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(supplierFromAnotherOrg));

        assertThatThrownBy(() -> supplierService.findSupplierById(2L, testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this supplier");

        verify(supplierRepository).findById(2L);
        verify(supplierMapper, never()).toDto(any(Supplier.class));
    }

    // Search Tests

    @Test
    @DisplayName("Should search suppliers by name")
    void shouldSearchSuppliersByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> supplierPage = new PageImpl<>(List.of(testSupplier));

        when(supplierRepository.findByOrganizationIdAndNameContainingIgnoreCase(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(supplierPage);
        when(supplierMapper.toSummaryDto(any(Supplier.class))).thenReturn(supplierSummaryDto);

        Page<SupplierSummaryDto> result = supplierService.searchSuppliersByName("Test", testUser, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(supplierSummaryDto);
        verify(supplierRepository).findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "Test", pageable);
    }

    @Test
    @DisplayName("Should search suppliers by email")
    void shouldSearchSuppliersByEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> supplierPage = new PageImpl<>(List.of(testSupplier));

        when(supplierRepository.findByOrganizationIdAndEmailContainingIgnoreCase(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(supplierPage);
        when(supplierMapper.toSummaryDto(any(Supplier.class))).thenReturn(supplierSummaryDto);

        Page<SupplierSummaryDto> result = supplierService.searchSuppliersByEmail("john", testUser, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(supplierSummaryDto);
        verify(supplierRepository).findByOrganizationIdAndEmailContainingIgnoreCase(
                testOrganization.getId(), "john", pageable);
    }

    // Update Supplier Tests

    @Test
    @DisplayName("Should update supplier successfully")
    void shouldUpdateSupplier_WhenValidRequest() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDto);

        SupplierDto result = supplierService.updateSupplier(1L, updateRequest, testUser);

        assertThat(result).isEqualTo(supplierDto);
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).existsByOrganizationIdAndName(testOrganization.getId(), updateRequest.name());
        verify(supplierRepository).save(testSupplier);
        verify(supplierMapper).toDto(testSupplier);
    }

    @Test
    @DisplayName("Should update supplier with same name successfully")
    void shouldUpdateSupplier_WhenKeepingSameName() {
        UpdateSupplierRequest sameNameRequest = new UpdateSupplierRequest(
                "Test Supplier", // Same name
                "Updated Contact",
                "updated@supplier.com",
                "+1111111111",
                "789 Updated Street"
        );

        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);
        when(supplierMapper.toDto(any(Supplier.class))).thenReturn(supplierDto);

        SupplierDto result = supplierService.updateSupplier(1L, sameNameRequest, testUser);

        assertThat(result).isEqualTo(supplierDto);
        verify(supplierRepository).findById(1L);
        verify(supplierRepository, never()).existsByOrganizationIdAndName(anyLong(), anyString());
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    @DisplayName("Should throw ConflictException when updating to existing supplier name")
    void shouldThrowConflictException_WhenUpdatingToExistingName() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.existsByOrganizationIdAndName(anyLong(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> supplierService.updateSupplier(1L, updateRequest, testUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Supplier with name 'Updated Supplier' already exists in this organization");

        verify(supplierRepository).findById(1L);
        verify(supplierRepository).existsByOrganizationIdAndName(testOrganization.getId(), updateRequest.name());
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent supplier")
    void shouldThrowResourceNotFoundException_WhenUpdatingNonExistentSupplier() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.updateSupplier(999L, updateRequest, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 999");

        verify(supplierRepository).findById(999L);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    // Delete Supplier Tests

    @Test
    @DisplayName("Should delete supplier successfully")
    void shouldDeleteSupplier_WhenSupplierExists() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(testSupplier));

        supplierService.deleteSupplier(1L, testUser);

        verify(supplierRepository).findById(1L);
        verify(supplierRepository).delete(testSupplier);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent supplier")
    void shouldThrowResourceNotFoundException_WhenDeletingNonExistentSupplier() {
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.deleteSupplier(999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found with id: 999");

        verify(supplierRepository).findById(999L);
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when deleting supplier from different organization")
    void shouldThrowAccessDeniedException_WhenDeletingSupplierFromDifferentOrganization() {
        Supplier supplierFromAnotherOrg = new Supplier();
        setEntityId(supplierFromAnotherOrg, 2L);
        supplierFromAnotherOrg.setOrganization(anotherOrganization);

        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(supplierFromAnotherOrg));

        assertThatThrownBy(() -> supplierService.deleteSupplier(2L, testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to access this supplier");

        verify(supplierRepository).findById(2L);
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    // Edge Cases and Security Tests

    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> emptyPage = new PageImpl<>(List.of());

        when(supplierRepository.findByOrganizationIdAndNameContainingIgnoreCase(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<SupplierSummaryDto> result = supplierService.searchSuppliersByName("NonExistent", testUser, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(supplierRepository).findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "NonExistent", pageable);
    }

    @Test
    @DisplayName("Should enforce organization isolation in all operations")
    void shouldEnforceOrganizationIsolationInAllOperations() {
        User userFromAnotherOrg = new User();
        setEntityId(userFromAnotherOrg, 2L);
        userFromAnotherOrg.setOrganization(anotherOrganization);

        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(testSupplier));

        // Test find operation
        assertThatThrownBy(() -> supplierService.findSupplierById(1L, userFromAnotherOrg))
                .isInstanceOf(AccessDeniedException.class);

        // Test update operation
        assertThatThrownBy(() -> supplierService.updateSupplier(1L, updateRequest, userFromAnotherOrg))
                .isInstanceOf(AccessDeniedException.class);

        // Test delete operation
        assertThatThrownBy(() -> supplierService.deleteSupplier(1L, userFromAnotherOrg))
                .isInstanceOf(AccessDeniedException.class);

        verify(supplierRepository, times(3)).findById(1L);
    }
}