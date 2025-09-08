package com.safework.api.domain.asset.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.asset.dto.AssetDto;
import com.safework.api.domain.asset.dto.CreateAssetRequest;
import com.safework.api.domain.asset.dto.UpdateAssetRequest;
import com.safework.api.domain.asset.service.AssetService;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
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
import com.safework.api.security.UserPrincipal;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    private User mockUser;
    private Organization organization;
    private AssetDto assetDto;
    private CreateAssetRequest createRequest;
    private UpdateAssetRequest updateRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setName("Test Organization");

        mockUser = new User();
        mockUser.setEmail("admin@test.com");
        mockUser.setName("Admin User");
        mockUser.setRole(UserRole.ADMIN);
        mockUser.setOrganization(organization);

        assetDto = new AssetDto(1L, "ASSET-001", "Test Asset", "QR-001", "ACTIVE", 1L, null, 0);

        createRequest = new CreateAssetRequest(
            "ASSET-NEW",
            "New Asset",
            "QR-NEW",
            1L
        );

        updateRequest = new UpdateAssetRequest(
            "Updated Asset",
            null,
            "INACTIVE",
            0
        );
    }

    @Test
    void createAsset_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        given(assetService.createAsset(any(CreateAssetRequest.class), any(User.class)))
            .willReturn(assetDto);

        // When/Then
        mockMvc.perform(post("/v1/assets")
                .with(user(new UserPrincipal(mockUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.assetTag").value("ASSET-001"))
            .andExpect(jsonPath("$.name").value("Test Asset"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void createAsset_ShouldReturnNotFound_WhenAssetTypeNotFound() throws Exception {
        // Given
        given(assetService.createAsset(any(CreateAssetRequest.class), any(User.class)))
            .willThrow(new ResourceNotFoundException("AssetType not found with id: 1"));

        // When/Then
        mockMvc.perform(post("/v1/assets")
                .with(user(new UserPrincipal(mockUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAssetsByOrganization_ShouldReturnPagedAssets() throws Exception {
        // Given
        Page<AssetDto> assetsPage = new PageImpl<>(List.of(assetDto), PageRequest.of(0, 10), 1);
        given(assetService.findAllByOrganization(eq(1L), any())).willReturn(assetsPage);

        // When/Then
        mockMvc.perform(get("/v1/assets")
                .with(user(new UserPrincipal(mockUser)))
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].assetTag").value("ASSET-001"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getAssetById_ShouldReturnAsset_WhenExists() throws Exception {
        // Given
        given(assetService.findAssetById(eq(1L), any(User.class))).willReturn(assetDto);

        // When/Then
        mockMvc.perform(get("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.assetTag").value("ASSET-001"))
            .andExpect(jsonPath("$.name").value("Test Asset"));
    }

    @Test
    void getAssetById_ShouldReturnNotFound_WhenAssetDoesNotExist() throws Exception {
        // Given
        given(assetService.findAssetById(eq(1L), any(User.class)))
            .willThrow(new ResourceNotFoundException("Asset not found with id: 1"));

        // When/Then
        mockMvc.perform(get("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAssetById_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        // Given
        given(assetService.findAssetById(eq(1L), any(User.class)))
            .willThrow(new AccessDeniedException("You do not have permission to access this asset."));

        // When/Then
        mockMvc.perform(get("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAssetByQrCode_ShouldReturnAsset_WhenExists() throws Exception {
        // Given
        given(assetService.findAssetByQrCode(eq("QR-001"), any(User.class))).willReturn(assetDto);

        // When/Then
        mockMvc.perform(get("/v1/assets/qr/QR-001")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.assetTag").value("ASSET-001"));
    }

    @Test
    void getAssetByQrCode_ShouldReturnNotFound_WhenQrCodeDoesNotExist() throws Exception {
        // Given
        given(assetService.findAssetByQrCode(eq("INVALID-QR"), any(User.class)))
            .willThrow(new ResourceNotFoundException("Asset not found with QR code: INVALID-QR"));

        // When/Then
        mockMvc.perform(get("/v1/assets/qr/INVALID-QR")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateAsset_ShouldReturnUpdatedAsset_WhenValidRequest() throws Exception {
        // Given
        AssetDto updatedDto = new AssetDto(1L, "ASSET-001", "Updated Asset", "QR-001", "INACTIVE", 1L, null, 1);
        given(assetService.updateAsset(eq(1L), any(UpdateAssetRequest.class), any(User.class)))
            .willReturn(updatedDto);

        // When/Then
        mockMvc.perform(put("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated Asset"))
            .andExpect(jsonPath("$.status").value("INACTIVE"))
            .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void updateAsset_ShouldReturnConflict_WhenVersionMismatch() throws Exception {
        // Given
        given(assetService.updateAsset(eq(1L), any(UpdateAssetRequest.class), any(User.class)))
            .willThrow(new ConflictException("Conflict: Asset has been updated by another user. Please refresh and try again."));

        // When/Then
        mockMvc.perform(put("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isConflict());
    }

    @Test
    void updateAsset_ShouldReturnNotFound_WhenAssetDoesNotExist() throws Exception {
        // Given
        given(assetService.updateAsset(eq(1L), any(UpdateAssetRequest.class), any(User.class)))
            .willThrow(new ResourceNotFoundException("Asset not found with id: 1"));

        // When/Then
        mockMvc.perform(put("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteAsset_ShouldReturnNoContent_WhenAssetExists() throws Exception {
        // When/Then
        mockMvc.perform(delete("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteAsset_ShouldReturnNotFound_WhenAssetDoesNotExist() throws Exception {
        // Given
        willThrow(new ResourceNotFoundException("Asset not found with id: 1"))
            .given(assetService).deleteAsset(eq(1L), any(User.class));

        // When/Then
        mockMvc.perform(delete("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteAsset_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        // Given
        willThrow(new AccessDeniedException("You do not have permission to access this asset."))
            .given(assetService).deleteAsset(eq(1L), any(User.class));

        // When/Then
        mockMvc.perform(delete("/v1/assets/1")
                .with(user(new UserPrincipal(mockUser))))
            .andExpect(status().isForbidden());
    }
}