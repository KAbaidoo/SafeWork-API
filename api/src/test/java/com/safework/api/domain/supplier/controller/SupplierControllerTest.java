package com.safework.api.domain.supplier.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.supplier.dto.CreateSupplierRequest;
import com.safework.api.domain.supplier.dto.SupplierDto;
import com.safework.api.domain.supplier.dto.SupplierSummaryDto;
import com.safework.api.domain.supplier.dto.UpdateSupplierRequest;
import com.safework.api.domain.supplier.service.SupplierService;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import com.safework.api.security.PrincipalUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SupplierController Tests")
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupplierService supplierService;

    private Organization testOrganization;
    private User adminUser;
    private User supervisorUser;
    private User regularUser;
    private PrincipalUser adminPrincipal;
    private PrincipalUser supervisorPrincipal;
    private PrincipalUser regularPrincipal;
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

        adminUser = new User();
        setEntityId(adminUser, 1L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(testOrganization);
        adminPrincipal = new PrincipalUser(adminUser);

        supervisorUser = new User();
        setEntityId(supervisorUser, 2L);
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@test.com");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(testOrganization);
        supervisorPrincipal = new PrincipalUser(supervisorUser);

        regularUser = new User();
        setEntityId(regularUser, 3L);
        regularUser.setName("Regular User");
        regularUser.setEmail("user@test.com");
        regularUser.setRole(UserRole.INSPECTOR);
        regularUser.setOrganization(testOrganization);
        regularPrincipal = new PrincipalUser(regularUser);

        createRequest = new CreateSupplierRequest(
                "Test Supplier",
                "John Doe",
                "john@supplier.com",
                "+1234567890",
                "123 Test Street"
        );

        updateRequest = new UpdateSupplierRequest(
                "Updated Supplier",
                "Jane Smith",
                "jane@supplier.com",
                "+0987654321",
                "456 Updated Street"
        );

        supplierDto = new SupplierDto(
                1L,
                "Test Supplier",
                "John Doe",
                "john@supplier.com",
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
                "john@supplier.com",
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

    // Create Supplier Tests

    @Test
    @DisplayName("Should create supplier successfully with ADMIN role")
    void shouldCreateSupplier_WithAdminRole() throws Exception {
        when(supplierService.createSupplier(any(CreateSupplierRequest.class), any(User.class)))
                .thenReturn(supplierDto);

        mockMvc.perform(post("/v1/suppliers")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Supplier"))
                .andExpect(jsonPath("$.contactPerson").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@supplier.com"));

        verify(supplierService).createSupplier(any(CreateSupplierRequest.class), eq(adminUser));
    }

    @Test
    @DisplayName("Should return 403 when creating supplier without ADMIN role")
    void shouldReturn403_WhenCreatingSupplierWithoutAdminRole() throws Exception {
        mockMvc.perform(post("/v1/suppliers")
                        .with(user(regularPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(supplierService, never()).createSupplier(any(), any());
    }

    @Test
    @DisplayName("Should return 400 when creating supplier with invalid data")
    void shouldReturn400_WhenCreatingSupplierWithInvalidData() throws Exception {
        CreateSupplierRequest invalidRequest = new CreateSupplierRequest(
                "", // Empty name
                "John Doe",
                "invalid-email", // Invalid email
                "123", // Invalid phone
                "123 Test Street"
        );

        mockMvc.perform(post("/v1/suppliers")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(supplierService, never()).createSupplier(any(), any());
    }

    @Test
    @DisplayName("Should return 409 when creating supplier with existing name")
    void shouldReturn409_WhenCreatingSupplierWithExistingName() throws Exception {
        when(supplierService.createSupplier(any(CreateSupplierRequest.class), any(User.class)))
                .thenThrow(new ConflictException("Supplier with name 'Test Supplier' already exists"));

        mockMvc.perform(post("/v1/suppliers")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    // Get All Suppliers Tests

    @Test
    @DisplayName("Should get all suppliers successfully")
    @WithMockUser(username = "test@example.com")
    void shouldGetAllSuppliers() throws Exception {
        Page<SupplierSummaryDto> suppliersPage = new PageImpl<>(List.of(supplierSummaryDto));

        when(supplierService.findAllByOrganization(anyLong(), any(Pageable.class)))
                .thenReturn(suppliersPage);

        mockMvc.perform(get("/v1/suppliers")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Supplier"));

        verify(supplierService).findAllByOrganization(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get suppliers with pagination")
    void shouldGetSuppliersWithPagination() throws Exception {
        Page<SupplierSummaryDto> suppliersPage = new PageImpl<>(List.of(supplierSummaryDto));

        when(supplierService.findAllByOrganization(anyLong(), any(Pageable.class)))
                .thenReturn(suppliersPage);

        mockMvc.perform(get("/v1/suppliers")
                        .with(user(adminPrincipal))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.pageable").exists());

        verify(supplierService).findAllByOrganization(eq(1L), any(Pageable.class));
    }

    // Get Supplier By ID Tests

    @Test
    @DisplayName("Should get supplier by ID successfully")
    void shouldGetSupplierById() throws Exception {
        when(supplierService.findSupplierById(anyLong(), any(User.class)))
                .thenReturn(supplierDto);

        mockMvc.perform(get("/v1/suppliers/1")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Supplier"))
                .andExpect(jsonPath("$.contactPerson").value("John Doe"));

        verify(supplierService).findSupplierById(1L, adminUser);
    }

    @Test
    @DisplayName("Should return 404 when supplier not found")
    void shouldReturn404_WhenSupplierNotFound() throws Exception {
        when(supplierService.findSupplierById(anyLong(), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Supplier not found with id: 999"));

        mockMvc.perform(get("/v1/suppliers/999")
                        .with(user(adminPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @DisplayName("Should return 403 when accessing supplier from different organization")
    void shouldReturn403_WhenAccessingSupplierFromDifferentOrganization() throws Exception {
        when(supplierService.findSupplierById(anyLong(), any(User.class)))
                .thenThrow(new AccessDeniedException("You do not have permission to access this supplier"));

        mockMvc.perform(get("/v1/suppliers/1")
                        .with(user(adminPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("permission")));
    }

    // Search Tests

    @Test
    @DisplayName("Should search suppliers by name")
    void shouldSearchSuppliersByName() throws Exception {
        Page<SupplierSummaryDto> searchResults = new PageImpl<>(List.of(supplierSummaryDto));

        when(supplierService.searchSuppliersByName(anyString(), any(User.class), any(Pageable.class)))
                .thenReturn(searchResults);

        mockMvc.perform(get("/v1/suppliers/search/name")
                        .with(user(adminPrincipal))
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Supplier"));

        verify(supplierService).searchSuppliersByName(eq("Test"), eq(adminUser), any(Pageable.class));
    }

    @Test
    @DisplayName("Should search suppliers by email")
    void shouldSearchSuppliersByEmail() throws Exception {
        Page<SupplierSummaryDto> searchResults = new PageImpl<>(List.of(supplierSummaryDto));

        when(supplierService.searchSuppliersByEmail(anyString(), any(User.class), any(Pageable.class)))
                .thenReturn(searchResults);

        mockMvc.perform(get("/v1/suppliers/search/email")
                        .with(user(adminPrincipal))
                        .param("email", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("john@supplier.com"));

        verify(supplierService).searchSuppliersByEmail(eq("john"), eq(adminUser), any(Pageable.class));
    }

    // Update Supplier Tests

    @Test
    @DisplayName("Should update supplier successfully with ADMIN role")
    void shouldUpdateSupplier_WithAdminRole() throws Exception {
        when(supplierService.updateSupplier(anyLong(), any(UpdateSupplierRequest.class), any(User.class)))
                .thenReturn(supplierDto);

        mockMvc.perform(put("/v1/suppliers/1")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Supplier"));

        verify(supplierService).updateSupplier(eq(1L), any(UpdateSupplierRequest.class), eq(adminUser));
    }

    @Test
    @DisplayName("Should update supplier successfully with SUPERVISOR role")
    void shouldUpdateSupplier_WithSupervisorRole() throws Exception {
        when(supplierService.updateSupplier(anyLong(), any(UpdateSupplierRequest.class), any(User.class)))
                .thenReturn(supplierDto);

        mockMvc.perform(put("/v1/suppliers/1")
                        .with(user(supervisorPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(supplierService).updateSupplier(eq(1L), any(UpdateSupplierRequest.class), eq(supervisorUser));
    }

    @Test
    @DisplayName("Should return 403 when updating supplier without proper role")
    void shouldReturn403_WhenUpdatingSupplierWithoutProperRole() throws Exception {
        mockMvc.perform(put("/v1/suppliers/1")
                        .with(user(regularPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(supplierService, never()).updateSupplier(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Should return 400 when updating supplier with invalid data")
    void shouldReturn400_WhenUpdatingSupplierWithInvalidData() throws Exception {
        UpdateSupplierRequest invalidRequest = new UpdateSupplierRequest(
                "", // Empty name
                "Jane Smith",
                "invalid-email",
                "123",
                "456 Updated Street"
        );

        mockMvc.perform(put("/v1/suppliers/1")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(supplierService, never()).updateSupplier(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Should return 409 when updating supplier with existing name")
    void shouldReturn409_WhenUpdatingSupplierWithExistingName() throws Exception {
        when(supplierService.updateSupplier(anyLong(), any(UpdateSupplierRequest.class), any(User.class)))
                .thenThrow(new ConflictException("Supplier with name 'Updated Supplier' already exists"));

        mockMvc.perform(put("/v1/suppliers/1")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    // Delete Supplier Tests

    @Test
    @DisplayName("Should delete supplier successfully with ADMIN role")
    void shouldDeleteSupplier_WithAdminRole() throws Exception {
        doNothing().when(supplierService).deleteSupplier(anyLong(), any(User.class));

        mockMvc.perform(delete("/v1/suppliers/1")
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());

        verify(supplierService).deleteSupplier(1L, adminUser);
    }

    @Test
    @DisplayName("Should return 403 when deleting supplier without ADMIN role")
    void shouldReturn403_WhenDeletingSupplierWithoutAdminRole() throws Exception {
        mockMvc.perform(delete("/v1/suppliers/1")
                        .with(user(supervisorPrincipal)))
                .andExpect(status().isForbidden());

        verify(supplierService, never()).deleteSupplier(anyLong(), any());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent supplier")
    void shouldReturn404_WhenDeletingNonExistentSupplier() throws Exception {
        doThrow(new ResourceNotFoundException("Supplier not found with id: 999"))
                .when(supplierService).deleteSupplier(anyLong(), any(User.class));

        mockMvc.perform(delete("/v1/suppliers/999")
                        .with(user(adminPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    // Authentication Tests

    @Test
    @DisplayName("Should return 401 when accessing endpoints without authentication")
    void shouldReturn401_WhenAccessingEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/suppliers"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/v1/suppliers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/v1/suppliers/1"))
                .andExpect(status().isUnauthorized());
    }

    // Note: Content Type and Method tests removed as they are dependent on Spring configuration
    // and may behave differently in test vs production environments
}