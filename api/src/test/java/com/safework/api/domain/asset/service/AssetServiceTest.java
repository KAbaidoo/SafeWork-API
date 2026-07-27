package com.safework.api.domain.asset.service;

import com.safework.api.domain.asset.dto.AssetDto;
import com.safework.api.domain.asset.dto.CreateAssetRequest;
import com.safework.api.domain.asset.dto.UpdateAssetRequest;
import com.safework.api.domain.asset.mapper.AssetMapper;
import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.asset.repository.AssetTypeRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetTypeRepository assetTypeRepository;

    @Mock
    private AssetMapper assetMapper;

    @InjectMocks
    private AssetService assetService;

    private User currentUser;
    private Organization organization;
    private Organization otherOrganization;
    private AssetType assetType;
    private Asset asset;
    private AssetDto assetDto;
    private CreateAssetRequest createRequest;
    private UpdateAssetRequest updateRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        // Note: For mock objects, we need to set ID for the test
        organization = createMockOrganization(1L, "Test Organization");

        otherOrganization = new Organization();
        // Note: For mock objects, we need to set ID for the test
        otherOrganization = createMockOrganization(2L, "Other Organization");

        currentUser = new User();
        // Note: For mock objects, we need to set up proper relationships
        setEntityId(currentUser, 1L);
        currentUser.setEmail("user@test.com");
        currentUser.setName("Test User");
        currentUser.setRole(UserRole.ADMIN);
        currentUser.setOrganization(organization);

        assetType = new AssetType();
        // Note: For mock objects, we need to set up proper relationships
        setEntityId(assetType, 1L);
        assetType.setName("Equipment");
        assetType.setOrganization(organization);

        asset = new Asset();
        // Note: For mock objects, we need to set up proper relationships
        setEntityId(asset, 1L);
        asset.setAssetTag("ASSET-001");
        asset.setName("Test Asset");
        asset.setQrCodeId("QR-001");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setVersion(0);

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

    /**
     * Helper method to create a mock organization with ID set via reflection
     * since the setter is protected in the entity.
     */
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

    /**
     * Helper method to set ID on any entity using reflection
     */
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
    void createAsset_ShouldCreateAsset_WhenValidRequest() {
        // Given
        Asset savedAsset = new Asset();
        // Note: IDs are auto-generated
        savedAsset.setAssetTag("ASSET-NEW");
        savedAsset.setName("New Asset");
        savedAsset.setOrganization(organization);
        savedAsset.setAssetType(assetType);
        savedAsset.setStatus(AssetStatus.INACTIVE);

        given(assetTypeRepository.findById(1L)).willReturn(Optional.of(assetType));
        given(assetRepository.save(any(Asset.class))).willReturn(savedAsset);
        given(assetMapper.toDto(savedAsset)).willReturn(assetDto);

        // When
        AssetDto result = assetService.createAsset(createRequest, currentUser);

        // Then
        assertThat(result).isEqualTo(assetDto);

        then(assetTypeRepository).should().findById(1L);
        then(assetRepository).should().save(any(Asset.class));
        then(assetMapper).should().toDto(savedAsset);
    }

    @Test
    void createAsset_ShouldThrowResourceNotFoundException_WhenAssetTypeNotFound() {
        // Given
        given(assetTypeRepository.findById(1L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> assetService.createAsset(createRequest, currentUser))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("AssetType not found with id: 1");

        then(assetRepository).should(never()).save(any(Asset.class));
        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void createAsset_ShouldSetDefaultStatusToInactive() {
        // Given
        given(assetTypeRepository.findById(1L)).willReturn(Optional.of(assetType));
        given(assetRepository.save(any(Asset.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(assetMapper.toDto(any(Asset.class))).willReturn(assetDto);

        // When
        assetService.createAsset(createRequest, currentUser);

        // Then
        then(assetRepository).should().save(any(Asset.class));
        // Verify the asset saved has INACTIVE status
        then(assetRepository).should().save(org.mockito.ArgumentMatchers.argThat(asset -> 
            asset.getStatus() == AssetStatus.INACTIVE
        ));
    }

    @Test
    void findAllByOrganization_ShouldReturnPagedAssets() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Asset> assets = List.of(asset);
        Page<Asset> assetPage = new PageImpl<>(assets, pageable, 1);
        
        given(assetRepository.findAllByOrganizationId(1L, pageable)).willReturn(assetPage);
        given(assetMapper.toDto(asset)).willReturn(assetDto);

        // When
        Page<AssetDto> result = assetService.findAllByOrganization(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(assetDto);
        assertThat(result.getTotalElements()).isEqualTo(1);

        then(assetRepository).should().findAllByOrganizationId(1L, pageable);
        then(assetMapper).should().toDto(asset);
    }

    @Test
    void findAssetById_ShouldReturnAsset_WhenAssetExists() {
        // Given
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(assetMapper.toDto(asset)).willReturn(assetDto);

        // When
        AssetDto result = assetService.findAssetById(1L, currentUser);

        // Then
        assertThat(result).isEqualTo(assetDto);

        then(assetRepository).should().findById(1L);
        then(assetMapper).should().toDto(asset);
    }

    @Test
    void findAssetById_ShouldThrowResourceNotFoundException_WhenAssetNotFound() {
        // Given
        given(assetRepository.findById(1L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> assetService.findAssetById(1L, currentUser))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Asset not found with id: 1");

        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void findAssetById_ShouldThrowAccessDeniedException_WhenUserNotFromSameOrganization() {
        // Given
        asset.setOrganization(otherOrganization);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));

        // When/Then
        assertThatThrownBy(() -> assetService.findAssetById(1L, currentUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You do not have permission to access this asset.");

        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void findAssetByQrCode_ShouldReturnAsset_WhenQrCodeExists() {
        // Given
        given(assetRepository.findByQrCodeId("QR-001")).willReturn(Optional.of(asset));
        given(assetMapper.toDto(asset)).willReturn(assetDto);

        // When
        AssetDto result = assetService.findAssetByQrCode("QR-001", currentUser);

        // Then
        assertThat(result).isEqualTo(assetDto);

        then(assetRepository).should().findByQrCodeId("QR-001");
        then(assetMapper).should().toDto(asset);
    }

    @Test
    void findAssetByQrCode_ShouldThrowResourceNotFoundException_WhenQrCodeNotFound() {
        // Given
        given(assetRepository.findByQrCodeId("INVALID-QR")).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> assetService.findAssetByQrCode("INVALID-QR", currentUser))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Asset not found with QR code: INVALID-QR");

        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void findAssetByQrCode_ShouldThrowAccessDeniedException_WhenUserNotFromSameOrganization() {
        // Given
        asset.setOrganization(otherOrganization);
        given(assetRepository.findByQrCodeId("QR-001")).willReturn(Optional.of(asset));

        // When/Then
        assertThatThrownBy(() -> assetService.findAssetByQrCode("QR-001", currentUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You do not have permission to access this asset.");

        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void updateAsset_ShouldUpdateAsset_WhenValidRequest() {
        // Given
        Asset updatedAsset = new Asset();
        // Note: IDs are auto-generated
        updatedAsset.setName("Updated Asset");
        updatedAsset.setStatus(AssetStatus.INACTIVE);
        updatedAsset.setVersion(1);

        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(assetRepository.save(asset)).willReturn(updatedAsset);
        given(assetMapper.toDto(updatedAsset)).willReturn(assetDto);

        // When
        AssetDto result = assetService.updateAsset(1L, updateRequest, currentUser);

        // Then
        assertThat(result).isEqualTo(assetDto);
        assertThat(asset.getName()).isEqualTo("Updated Asset");
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.INACTIVE);

        then(assetRepository).should().findById(1L);
        then(assetRepository).should().save(asset);
        then(assetMapper).should().toDto(updatedAsset);
    }

    @Test
    void updateAsset_ShouldThrowConflictException_WhenVersionMismatch() {
        // Given
        asset.setVersion(1); // Different version than in request (0)
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));

        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(1L, updateRequest, currentUser))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Conflict: Asset has been updated by another user. Please refresh and try again.");

        then(assetRepository).should(never()).save(any(Asset.class));
        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void updateAsset_ShouldThrowResourceNotFoundException_WhenAssetNotFound() {
        // Given
        given(assetRepository.findById(1L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(1L, updateRequest, currentUser))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Asset not found with id: 1");

        then(assetRepository).should(never()).save(any(Asset.class));
        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void updateAsset_ShouldThrowAccessDeniedException_WhenUserNotFromSameOrganization() {
        // Given
        asset.setOrganization(otherOrganization);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));

        // When/Then
        assertThatThrownBy(() -> assetService.updateAsset(1L, updateRequest, currentUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You do not have permission to access this asset.");

        then(assetRepository).should(never()).save(any(Asset.class));
        then(assetMapper).should(never()).toDto(any(Asset.class));
    }

    @Test
    void deleteAsset_ShouldDeleteAsset_WhenAssetExists() {
        // Given
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));

        // When
        assetService.deleteAsset(1L, currentUser);

        // Then
        then(assetRepository).should().findById(1L);
        then(assetRepository).should().delete(asset);
    }

    @Test
    void deleteAsset_ShouldThrowResourceNotFoundException_WhenAssetNotFound() {
        // Given
        given(assetRepository.findById(1L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> assetService.deleteAsset(1L, currentUser))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Asset not found with id: 1");

        then(assetRepository).should(never()).delete(any(Asset.class));
    }

    @Test
    void deleteAsset_ShouldThrowAccessDeniedException_WhenUserNotFromSameOrganization() {
        // Given
        asset.setOrganization(otherOrganization);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));

        // When/Then
        assertThatThrownBy(() -> assetService.deleteAsset(1L, currentUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You do not have permission to access this asset.");

        then(assetRepository).should(never()).delete(any(Asset.class));
    }
}