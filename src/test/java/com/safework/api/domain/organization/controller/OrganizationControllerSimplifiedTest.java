package com.safework.api.domain.organization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.organization.dto.CreateOrganizationRequest;
import com.safework.api.domain.organization.dto.OrganizationDto;
import com.safework.api.domain.organization.dto.UpdateOrganizationRequest;
import com.safework.api.domain.organization.service.OrganizationService;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.security.PrincipalUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganizationControllerSimplifiedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    private User adminUser;
    private User supervisorUser;
    private User inspectorUser;
    private Organization organization;
    private OrganizationDto organizationDto;
    private CreateOrganizationRequest createRequest;
    private UpdateOrganizationRequest updateRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization = createMockOrganization(1L, "Test Organization");

        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@testorg.com");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);
        setEntityId(adminUser, 1L);

        supervisorUser = new User();
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@testorg.com");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(organization);
        setEntityId(supervisorUser, 2L);

        inspectorUser = new User();
        inspectorUser.setName("Inspector User");
        inspectorUser.setEmail("inspector@testorg.com");
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(organization);
        setEntityId(inspectorUser, 3L);

        organizationDto = new OrganizationDto(
                1L, "Test Organization", "123 Test Street", "+1-555-0123",
                "https://testorg.com", "Technology", "MEDIUM",
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new CreateOrganizationRequest(
                "New Organization",
                "456 New Street",
                "+1-555-0456",
                "https://neworg.com",
                "Manufacturing",
                "LARGE"
        );

        updateRequest = new UpdateOrganizationRequest(
                "Updated Organization",
                "789 Updated Street",
                "+1-555-0789",
                "https://updatedorg.com",
                "Healthcare",
                "ENTERPRISE"
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

    // Since SUPER_ADMIN doesn't exist in UserRole enum, all operations requiring SUPER_ADMIN will return forbidden

    @Test
    void createOrganization_ShouldReturnForbidden_WhenAdminTriesToCreate() throws Exception {
        // When & Then - ADMIN doesn't have SUPER_ADMIN authority
        mockMvc.perform(post("/v1/organizations")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrganizations_ShouldReturnForbidden_WhenAdminRequests() throws Exception {
        // When & Then - ADMIN doesn't have SUPER_ADMIN authority
        mockMvc.perform(get("/v1/organizations")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentUserOrganization_ShouldReturnOrganization_WhenAnyAuthenticatedUser() throws Exception {
        // Given
        given(organizationService.getCurrentUserOrganization(adminUser)).willReturn(organizationDto);

        // When & Then - This endpoint doesn't require SUPER_ADMIN
        mockMvc.perform(get("/v1/organizations/me")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Organization"));
    }

    @Test
    void getCurrentUserOrganization_ShouldReturnOrganization_WhenInspectorRequests() throws Exception {
        // Given
        given(organizationService.getCurrentUserOrganization(inspectorUser)).willReturn(organizationDto);

        // When & Then - This endpoint is accessible to all authenticated users
        mockMvc.perform(get("/v1/organizations/me")
                        .with(user(new PrincipalUser(inspectorUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateCurrentUserOrganization_ShouldUpdateOrganization_WhenAdminMakesValidRequest() throws Exception {
        // Given
        given(organizationService.updateOrganization(eq(1L), any(UpdateOrganizationRequest.class), eq(adminUser)))
                .willReturn(organizationDto);

        // When & Then - This endpoint requires ADMIN authority which exists
        mockMvc.perform(put("/v1/organizations/me")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateCurrentUserOrganization_ShouldReturnForbidden_WhenSupervisorTriesToUpdate() throws Exception {
        // When & Then - SUPERVISOR doesn't have ADMIN authority
        mockMvc.perform(put("/v1/organizations/me")
                        .with(user(new PrincipalUser(supervisorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCurrentUserOrganization_ShouldReturnForbidden_WhenInspectorTriesToUpdate() throws Exception {
        // When & Then - INSPECTOR doesn't have ADMIN authority
        mockMvc.perform(put("/v1/organizations/me")
                        .with(user(new PrincipalUser(inspectorUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrganizationById_ShouldReturnForbidden_WhenAdminTriesToUpdate() throws Exception {
        // When & Then - ADMIN doesn't have SUPER_ADMIN authority
        mockMvc.perform(put("/v1/organizations/1")
                        .with(user(new PrincipalUser(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteOrganization_ShouldReturnForbidden_WhenAdminTriesToDelete() throws Exception {
        // When & Then - ADMIN doesn't have SUPER_ADMIN authority
        mockMvc.perform(delete("/v1/organizations/1")
                        .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrganization_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUserOrganization_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/organizations/me"))
                .andExpect(status().isUnauthorized());
    }
}