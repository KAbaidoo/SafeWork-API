package com.safework.api.domain.department.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.department.dto.CreateDepartmentRequest;
import com.safework.api.domain.department.dto.DepartmentDto;
import com.safework.api.domain.department.dto.DepartmentSummaryDto;
import com.safework.api.domain.department.dto.UpdateDepartmentRequest;
import com.safework.api.domain.department.service.DepartmentService;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import com.safework.api.security.PrincipalUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    private User adminUser;
    private User supervisorUser;
    private User inspectorUser;
    private Organization organization;
    private DepartmentDto departmentDto;
    private DepartmentSummaryDto departmentSummaryDto;
    private CreateDepartmentRequest createRequest;
    private UpdateDepartmentRequest updateRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization = createMockOrganization(1L, "Test Corporation");

        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@testcorp.com");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);
        setEntityId(adminUser, 1L);

        supervisorUser = new User();
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@testcorp.com");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(organization);
        setEntityId(supervisorUser, 2L);

        inspectorUser = new User();
        inspectorUser.setName("Inspector User");
        inspectorUser.setEmail("inspector@testcorp.com");
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(organization);
        setEntityId(inspectorUser, 3L);

        departmentDto = new DepartmentDto(
                1L, "Information Technology", "IT department", "IT",
                1L, 1L, "Admin User", 5,
                LocalDateTime.now(), LocalDateTime.now()
        );

        departmentSummaryDto = new DepartmentSummaryDto(
                1L, "Information Technology", "IT", "Admin User", 5
        );

        createRequest = new CreateDepartmentRequest(
                "New Department",
                "New department description",
                "NEWDEPT",
                1L
        );

        updateRequest = new UpdateDepartmentRequest(
                "Updated Department",
                "Updated description",
                "UPDEPT",
                1L
        );
    }

    private Organization createMockOrganization(Long id, String name) {
        Organization org = new Organization();
        org.setName(name);
        try {
            Field idField = Organization.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(org, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set organization ID", e);
        }
        return org;
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

    // CREATE DEPARTMENT TESTS

    @Test
    void createDepartment_ShouldCreateDepartment_WhenAdminMakesValidRequest() throws Exception {
        // Given
        given(departmentService.createDepartment(any(CreateDepartmentRequest.class), eq(adminUser)))
                .willReturn(departmentDto);

        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Information Technology"))
                .andExpect(jsonPath("$.description").value("IT department"))
                .andExpect(jsonPath("$.code").value("IT"))
                .andExpect(jsonPath("$.organizationId").value(1))
                .andExpect(jsonPath("$.managerId").value(1))
                .andExpect(jsonPath("$.managerName").value("Admin User"))
                .andExpect(jsonPath("$.employeeCount").value(5));
    }

    @Test
    void createDepartment_ShouldReturnForbidden_WhenSupervisorTriesToCreate() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .with(user(new PrincipalUser(supervisorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepartment_ShouldReturnForbidden_WhenInspectorTriesToCreate() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .with(user(new PrincipalUser(inspectorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepartment_ShouldReturnConflict_WhenDepartmentNameExists() throws Exception {
        // Given
        given(departmentService.createDepartment(any(CreateDepartmentRequest.class), eq(adminUser)))
                .willThrow(new ConflictException("Department with name 'New Department' already exists in this organization"));

        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Department with name 'New Department' already exists in this organization"));
    }

    @Test
    void createDepartment_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        CreateDepartmentRequest invalidRequest = new CreateDepartmentRequest(
                "", // Empty name
                "a".repeat(501), // Description too long
                "invalid-code", // Invalid code pattern
                null // null managerId is valid
        );

        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDepartment_ShouldAcceptValidCodePattern() throws Exception {
        // Given
        CreateDepartmentRequest validCodeRequest = new CreateDepartmentRequest(
                "Test Department",
                "Description",
                "TESTDEPT10", // Valid code pattern: uppercase letters and numbers, 10 chars
                1L
        );

        given(departmentService.createDepartment(any(CreateDepartmentRequest.class), eq(adminUser)))
                .willReturn(departmentDto);

        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCodeRequest)))
                .andExpect(status().isCreated());
    }

    // GET DEPARTMENTS TESTS

    @Test
    void getDepartmentsByOrganization_ShouldReturnPagedDepartments_WhenAdminRequests() throws Exception {
        // Given
        Page<DepartmentSummaryDto> departmentsPage = new PageImpl<>(
                List.of(departmentSummaryDto), PageRequest.of(0, 10), 1
        );
        given(departmentService.findAllByOrganization(eq(1L), any())).willReturn(departmentsPage);

        // When & Then
        mockMvc.perform(get("/v1/departments")
                        .with(user(new PrincipalUser(adminUser)))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Information Technology"))
                .andExpect(jsonPath("$.content[0].code").value("IT"))
                .andExpect(jsonPath("$.content[0].managerName").value("Admin User"))
                .andExpect(jsonPath("$.content[0].employeeCount").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getDepartmentsByOrganization_ShouldReturnDepartments_WhenSupervisorRequests() throws Exception {
        // Given
        Page<DepartmentSummaryDto> departmentsPage = new PageImpl<>(
                List.of(departmentSummaryDto), PageRequest.of(0, 10), 1
        );
        given(departmentService.findAllByOrganization(eq(1L), any())).willReturn(departmentsPage);

        // When & Then
        mockMvc.perform(get("/v1/departments")
                        .with(user(new PrincipalUser(supervisorUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Information Technology"));
    }

    @Test
    void getDepartmentsByOrganization_ShouldReturnDepartments_WhenInspectorRequests() throws Exception {
        // Given
        Page<DepartmentSummaryDto> departmentsPage = new PageImpl<>(
                List.of(departmentSummaryDto), PageRequest.of(0, 10), 1
        );
        given(departmentService.findAllByOrganization(eq(1L), any())).willReturn(departmentsPage);

        // When & Then
        mockMvc.perform(get("/v1/departments")
                        .with(user(new PrincipalUser(inspectorUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Information Technology"));
    }

    // GET DEPARTMENT BY ID TESTS

    @Test
    void getDepartmentById_ShouldReturnDepartment_WhenAdminRequests() throws Exception {
        // Given
        given(departmentService.findDepartmentById(1L, adminUser)).willReturn(departmentDto);

        // When & Then
        mockMvc.perform(get("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Information Technology"));
    }

    @Test
    void getDepartmentById_ShouldReturnDepartment_WhenInspectorRequests() throws Exception {
        // Given
        given(departmentService.findDepartmentById(1L, inspectorUser)).willReturn(departmentDto);

        // When & Then
        mockMvc.perform(get("/v1/departments/1")
                        .with(user(new PrincipalUser(inspectorUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Information Technology"));
    }

    @Test
    void getDepartmentById_ShouldReturnNotFound_WhenDepartmentDoesNotExist() throws Exception {
        // Given
        given(departmentService.findDepartmentById(999L, adminUser))
                .willThrow(new ResourceNotFoundException("Department not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/v1/departments/999")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Department not found with id: 999"));
    }

    @Test
    void getDepartmentById_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        // Given
        given(departmentService.findDepartmentById(1L, adminUser))
                .willThrow(new AccessDeniedException("You do not have permission to access this department"));

        // When & Then
        mockMvc.perform(get("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isForbidden());
    }

    // UPDATE DEPARTMENT TESTS

    @Test
    void updateDepartment_ShouldUpdateDepartment_WhenAdminMakesValidRequest() throws Exception {
        // Given
        given(departmentService.updateDepartment(eq(1L), any(UpdateDepartmentRequest.class), eq(adminUser)))
                .willReturn(departmentDto);

        // When & Then
        mockMvc.perform(put("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateDepartment_ShouldUpdateDepartment_WhenSupervisorMakesValidRequest() throws Exception {
        // Given
        given(departmentService.updateDepartment(eq(1L), any(UpdateDepartmentRequest.class), eq(supervisorUser)))
                .willReturn(departmentDto);

        // When & Then
        mockMvc.perform(put("/v1/departments/1")
                        .with(user(new PrincipalUser(supervisorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateDepartment_ShouldReturnForbidden_WhenInspectorTriesToUpdate() throws Exception {
        // When & Then
        mockMvc.perform(put("/v1/departments/1")
                        .with(user(new PrincipalUser(inspectorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDepartment_ShouldReturnConflict_WhenNameAlreadyExists() throws Exception {
        // Given
        given(departmentService.updateDepartment(eq(1L), any(UpdateDepartmentRequest.class), eq(adminUser)))
                .willThrow(new ConflictException("Department with name 'Updated Department' already exists in this organization"));

        // When & Then
        mockMvc.perform(put("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Department with name 'Updated Department' already exists in this organization"));
    }

    @Test
    void updateDepartment_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        UpdateDepartmentRequest invalidRequest = new UpdateDepartmentRequest(
                "a", // Name too short
                "a".repeat(501), // Description too long
                "invalid-code", // Invalid code pattern
                null // null managerId is valid
        );

        // When & Then
        mockMvc.perform(put("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // DELETE DEPARTMENT TESTS

    @Test
    void deleteDepartment_ShouldDeleteDepartment_WhenAdminMakesValidRequest() throws Exception {
        // Given
        doNothing().when(departmentService).deleteDepartment(1L, adminUser);

        // When & Then
        mockMvc.perform(delete("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDepartment_ShouldReturnForbidden_WhenSupervisorTriesToDelete() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/departments/1")
                        .with(user(new PrincipalUser(supervisorUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDepartment_ShouldReturnForbidden_WhenInspectorTriesToDelete() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/departments/1")
                        .with(user(new PrincipalUser(inspectorUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDepartment_ShouldReturnNotFound_WhenDepartmentDoesNotExist() throws Exception {
        // Given
        willThrow(new ResourceNotFoundException("Department not found with id: 999"))
                .given(departmentService).deleteDepartment(999L, adminUser);

        // When & Then
        mockMvc.perform(delete("/v1/departments/999")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Department not found with id: 999"));
    }

    @Test
    void deleteDepartment_ShouldReturnConflict_WhenDepartmentHasEmployees() throws Exception {
        // Given
        willThrow(new ConflictException("Cannot delete department with employees. Please reassign employees first."))
                .given(departmentService).deleteDepartment(1L, adminUser);

        // When & Then
        mockMvc.perform(delete("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete department with employees. Please reassign employees first."));
    }

    // UNAUTHORIZED TESTS

    @Test
    void createDepartment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDepartmentsByOrganization_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/departments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDepartmentById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/departments/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateDepartment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(put("/v1/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteDepartment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/departments/1"))
                .andExpect(status().isUnauthorized());
    }

    // VALIDATION TESTS

    @Test
    void createDepartment_ShouldAcceptNullOptionalFields() throws Exception {
        // Given
        CreateDepartmentRequest requestWithNulls = new CreateDepartmentRequest(
                "Valid Department Name",
                null, // null description is valid
                null, // null code is valid
                null  // null managerId is valid
        );

        given(departmentService.createDepartment(any(CreateDepartmentRequest.class), eq(adminUser)))
                .willReturn(departmentDto);

        // When & Then
        mockMvc.perform(post("/v1/departments")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNulls)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateDepartment_ShouldAcceptNullOptionalFields() throws Exception {
        // Given
        UpdateDepartmentRequest requestWithNulls = new UpdateDepartmentRequest(
                "Valid Department Name",
                null, // null description is valid
                null, // null code is valid
                null  // null managerId is valid
        );

        given(departmentService.updateDepartment(eq(1L), any(UpdateDepartmentRequest.class), eq(adminUser)))
                .willReturn(departmentDto);

        // When & Then
        mockMvc.perform(put("/v1/departments/1")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNulls)))
                .andExpect(status().isOk());
    }
}