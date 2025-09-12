package com.safework.api.domain.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.location.dto.*;
import com.safework.api.domain.location.service.LocationService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User supervisorUser;
    private User inspectorUser;
    private Organization organization;
    private PrincipalUser adminPrincipal;
    private PrincipalUser supervisorPrincipal;
    private PrincipalUser inspectorPrincipal;
    private LocationDto locationDto;
    private LocationSummaryDto locationSummaryDto;
    private LocationWithAssetsDto locationWithAssetsDto;

    @BeforeEach
    void setUp() {
        // Create organization
        organization = new Organization();
        organization.setName("Test Organization");
        setEntityId(organization, 1L);

        // Create users
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

        // Create principal users
        adminPrincipal = new PrincipalUser(adminUser);
        supervisorPrincipal = new PrincipalUser(supervisorUser);
        inspectorPrincipal = new PrincipalUser(inspectorUser);

        // Create DTOs
        locationDto = new LocationDto(
                1L, "Test Location", "Test description", "WAREHOUSE",
                "123 Test St", "Test City", "Test Country", "Building A",
                "1", "Zone A", new BigDecimal("40.7128"), new BigDecimal("-74.0060"),
                100, 25, true, 1L, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        locationSummaryDto = new LocationSummaryDto(
                1L, "Test Location", "WAREHOUSE", "Building A",
                "1", "Zone A", 25, 100, true
        );

        locationWithAssetsDto = new LocationWithAssetsDto(
                1L, "Test Location", "WAREHOUSE", "Test description",
                25, 100, true, Collections.emptyList()
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

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createLocation_ShouldReturnCreatedLocation_WhenValidRequestAsAdmin() throws Exception {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "New Location", "New description", "WAREHOUSE",
                "456 New St", "New City", "New Country", "Building B",
                "2", "Zone B", new BigDecimal("41.8781"), new BigDecimal("-87.6298"),
                150, null
        );

        when(locationService.createLocation(any(CreateLocationRequest.class), any(User.class)))
                .thenReturn(locationDto);

        // When & Then
        mockMvc.perform(post("/v1/locations")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Location"))
                .andExpect(jsonPath("$.locationType").value("WAREHOUSE"));
    }

    @Test
    @WithMockUser(authorities = "SUPERVISOR")
    void createLocation_ShouldReturnCreatedLocation_WhenValidRequestAsSupervisor() throws Exception {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "New Location", null, "OFFICE", null, null, null, null,
                null, null, null, null, null, null
        );

        when(locationService.createLocation(any(CreateLocationRequest.class), any(User.class)))
                .thenReturn(locationDto);

        // When & Then
        mockMvc.perform(post("/v1/locations")
                        .with(user(supervisorPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void createLocation_ShouldReturnForbidden_WhenInsufficientPermissions() throws Exception {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "New Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, null
        );

        // When & Then
        mockMvc.perform(post("/v1/locations")
                        .with(user(inspectorPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createLocation_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given - request with missing required fields
        CreateLocationRequest invalidRequest = new CreateLocationRequest(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );

        // When & Then
        mockMvc.perform(post("/v1/locations")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createLocation_ShouldReturnConflict_WhenLocationNameExists() throws Exception {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "Existing Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, null
        );

        when(locationService.createLocation(any(CreateLocationRequest.class), any(User.class)))
                .thenThrow(new ConflictException("Location with name 'Existing Location' already exists"));

        // When & Then
        mockMvc.perform(post("/v1/locations")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Location with name 'Existing Location' already exists"));
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void getLocationsByOrganization_ShouldReturnPaginatedLocations() throws Exception {
        // Given
        List<LocationSummaryDto> locations = Arrays.asList(locationSummaryDto);
        Page<LocationSummaryDto> locationPage = new PageImpl<>(locations, PageRequest.of(0, 10), 1);

        when(locationService.findAllByOrganization(anyLong(), any(Pageable.class)))
                .thenReturn(locationPage);

        // When & Then
        mockMvc.perform(get("/v1/locations")
                        .with(user(inspectorPrincipal))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Location"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void getLocationsByOrganization_ShouldReturnActiveOnly_WhenActiveOnlyParamIsTrue() throws Exception {
        // Given
        List<LocationSummaryDto> activeLocations = Arrays.asList(locationSummaryDto);
        Page<LocationSummaryDto> locationPage = new PageImpl<>(activeLocations, PageRequest.of(0, 10), 1);

        when(locationService.findActiveByOrganization(anyLong(), any(Pageable.class)))
                .thenReturn(locationPage);

        // When & Then
        mockMvc.perform(get("/v1/locations")
                        .with(user(inspectorPrincipal))
                        .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void getLocationById_ShouldReturnLocation_WhenLocationExists() throws Exception {
        // Given
        when(locationService.findLocationById(anyLong(), any(User.class)))
                .thenReturn(locationDto);

        // When & Then
        mockMvc.perform(get("/v1/locations/1")
                        .with(user(inspectorPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Location"))
                .andExpect(jsonPath("$.locationType").value("WAREHOUSE"));
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void getLocationById_ShouldReturnNotFound_WhenLocationDoesNotExist() throws Exception {
        // Given
        when(locationService.findLocationById(anyLong(), any(User.class)))
                .thenThrow(new ResourceNotFoundException("Location not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/v1/locations/999")
                        .with(user(inspectorPrincipal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found with id: 999"));
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void getLocationById_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        // Given
        when(locationService.findLocationById(anyLong(), any(User.class)))
                .thenThrow(new AccessDeniedException("You do not have permission to access this location"));

        // When & Then
        mockMvc.perform(get("/v1/locations/1")
                        .with(user(inspectorPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void getLocationWithAssets_ShouldReturnLocationWithAssets() throws Exception {
        // Given
        when(locationService.findLocationWithAssets(anyLong(), any(User.class)))
                .thenReturn(locationWithAssetsDto);

        // When & Then
        mockMvc.perform(get("/v1/locations/1/assets")
                        .with(user(inspectorPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Location"))
                .andExpect(jsonPath("$.assets").isArray());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateLocation_ShouldReturnUpdatedLocation_WhenValidRequestAsAdmin() throws Exception {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Updated Location", "Updated description", "OFFICE",
                "789 Updated St", "Updated City", "Updated Country", "Building C",
                "3", "Zone C", new BigDecimal("42.3601"), new BigDecimal("-71.0589"),
                200, true, null
        );

        LocationDto updatedLocationDto = new LocationDto(
                1L, "Updated Location", "Updated description", "OFFICE",
                "789 Updated St", "Updated City", "Updated Country", "Building C",
                "3", "Zone C", new BigDecimal("42.3601"), new BigDecimal("-71.0589"),
                200, 25, true, 1L, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(locationService.updateLocation(anyLong(), any(UpdateLocationRequest.class), any(User.class)))
                .thenReturn(updatedLocationDto);

        // When & Then
        mockMvc.perform(put("/v1/locations/1")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Location"))
                .andExpect(jsonPath("$.locationType").value("OFFICE"));
    }

    @Test
    @WithMockUser(authorities = "SUPERVISOR")
    void updateLocation_ShouldReturnUpdatedLocation_WhenValidRequestAsSupervisor() throws Exception {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Updated Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, true, null
        );

        when(locationService.updateLocation(anyLong(), any(UpdateLocationRequest.class), any(User.class)))
                .thenReturn(locationDto);

        // When & Then
        mockMvc.perform(put("/v1/locations/1")
                        .with(user(supervisorPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void updateLocation_ShouldReturnForbidden_WhenInsufficientPermissions() throws Exception {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Updated Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, true, null
        );

        // When & Then
        mockMvc.perform(put("/v1/locations/1")
                        .with(user(inspectorPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateLocation_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        UpdateLocationRequest invalidRequest = new UpdateLocationRequest(
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        // When & Then
        mockMvc.perform(put("/v1/locations/1")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateLocation_ShouldReturnConflict_WhenLocationNameExists() throws Exception {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Existing Name", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, true, null
        );

        when(locationService.updateLocation(anyLong(), any(UpdateLocationRequest.class), any(User.class)))
                .thenThrow(new ConflictException("Location with name 'Existing Name' already exists"));

        // When & Then
        mockMvc.perform(put("/v1/locations/1")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteLocation_ShouldReturnNoContent_WhenLocationDeletedSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/locations/1")
                        .with(user(adminPrincipal)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "SUPERVISOR")
    void deleteLocation_ShouldReturnForbidden_WhenInsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/locations/1")
                        .with(user(supervisorPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void deleteLocation_ShouldReturnForbidden_WhenInsufficientPermissionsAsInspector() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/locations/1")
                        .with(user(inspectorPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteLocation_ShouldReturnNotFound_WhenLocationDoesNotExist() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Location not found with id: 999"))
                .when(locationService).deleteLocation(anyLong(), any(User.class));

        // When & Then
        mockMvc.perform(delete("/v1/locations/999")
                        .with(user(adminPrincipal)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteLocation_ShouldReturnConflict_WhenLocationHasAssets() throws Exception {
        // Given
        doThrow(new ConflictException("Cannot delete location with 5 asset(s)"))
                .when(locationService).deleteLocation(anyLong(), any(User.class));

        // When & Then
        mockMvc.perform(delete("/v1/locations/1")
                        .with(user(adminPrincipal)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void transferAssetToLocation_ShouldReturnOk_WhenTransferSuccessful() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/locations/1/transfer-asset/1")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SUPERVISOR")
    void transferAssetToLocation_ShouldReturnOk_WhenTransferSuccessfulAsSupervisor() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/locations/1/transfer-asset/1")
                        .with(user(supervisorPrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void transferAssetToLocation_ShouldReturnForbidden_WhenInsufficientPermissions() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/locations/1/transfer-asset/1")
                        .with(user(inspectorPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void transferAssetToLocation_ShouldReturnConflict_WhenLocationAtCapacity() throws Exception {
        // Given
        doThrow(new ConflictException("Location is at capacity"))
                .when(locationService).transferAssetToLocation(anyLong(), anyLong(), any(User.class));

        // When & Then
        mockMvc.perform(post("/v1/locations/1/transfer-asset/1")
                        .with(user(adminPrincipal)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "INSPECTOR")
    void getAvailableLocations_ShouldReturnAvailableLocations() throws Exception {
        // Given
        List<LocationSummaryDto> availableLocations = Arrays.asList(locationSummaryDto);
        Page<LocationSummaryDto> locationPage = new PageImpl<>(availableLocations, PageRequest.of(0, 10), 1);

        when(locationService.findAvailableLocations(anyLong(), any(Pageable.class)))
                .thenReturn(locationPage);

        // When & Then
        mockMvc.perform(get("/v1/locations/available")
                        .with(user(inspectorPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void createLocation_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Given
        CreateLocationRequest request = new CreateLocationRequest(
                "New Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, null
        );

        // When & Then
        mockMvc.perform(post("/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLocationsByOrganization_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/locations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLocationById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/locations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLocationWithAssets_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/locations/1/assets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateLocation_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Given
        UpdateLocationRequest request = new UpdateLocationRequest(
                "Updated Location", null, "WAREHOUSE", null, null, null, null,
                null, null, null, null, null, true, null
        );

        // When & Then
        mockMvc.perform(put("/v1/locations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteLocation_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/locations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transferAssetToLocation_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/locations/1/transfer-asset/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAvailableLocations_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/locations/available"))
                .andExpect(status().isUnauthorized());
    }
}