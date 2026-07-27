package com.safework.api.domain.asset.repository;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.organization.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AssetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AssetRepository assetRepository;

    private Organization organization1;
    private Organization organization2;
    private AssetType assetType;
    private Asset asset1;
    private Asset asset2;
    private Asset asset3;

    @BeforeEach
    void setUp() {
        // Create test organizations
        organization1 = new Organization();
        organization1.setName("Org 1");
        entityManager.persistAndFlush(organization1);

        organization2 = new Organization();
        organization2.setName("Org 2");
        entityManager.persistAndFlush(organization2);

        // Create asset type
        assetType = new AssetType();
        assetType.setName("Equipment");
        assetType.setOrganization(organization1);
        entityManager.persistAndFlush(assetType);

        // Create test assets
        asset1 = new Asset();
        asset1.setAssetTag("ASSET-001");
        asset1.setName("Asset 1");
        asset1.setQrCodeId("QR-001");
        asset1.setOrganization(organization1);
        asset1.setAssetType(assetType);
        asset1.setStatus(AssetStatus.ACTIVE);
        entityManager.persistAndFlush(asset1);

        asset2 = new Asset();
        asset2.setAssetTag("ASSET-002");
        asset2.setName("Asset 2");
        asset2.setQrCodeId("QR-002");
        asset2.setOrganization(organization1);
        asset2.setAssetType(assetType);
        asset2.setStatus(AssetStatus.INACTIVE);
        entityManager.persistAndFlush(asset2);

        asset3 = new Asset();
        asset3.setAssetTag("ASSET-003");
        asset3.setName("Asset 3");
        asset3.setQrCodeId("QR-003");
        asset3.setOrganization(organization2);
        asset3.setAssetType(assetType);
        asset3.setStatus(AssetStatus.ACTIVE);
        entityManager.persistAndFlush(asset3);

        entityManager.clear();
    }

    @Test
    void findByQrCodeId_ShouldReturnAsset_WhenQrCodeExists() {
        // When
        Optional<Asset> result = assetRepository.findByQrCodeId("QR-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAssetTag()).isEqualTo("ASSET-001");
        assertThat(result.get().getName()).isEqualTo("Asset 1");
        assertThat(result.get().getQrCodeId()).isEqualTo("QR-001");
    }

    @Test
    void findByQrCodeId_ShouldReturnEmpty_WhenQrCodeDoesNotExist() {
        // When
        Optional<Asset> result = assetRepository.findByQrCodeId("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByQrCodeId_ShouldReturnEmpty_WhenQrCodeIsNull() {
        // When
        Optional<Asset> result = assetRepository.findByQrCodeId(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByOrganizationId_ShouldReturnAssetsForOrganization() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Asset> result = assetRepository.findAllByOrganizationId(organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Asset::getAssetTag)
                .containsExactlyInAnyOrder("ASSET-001", "ASSET-002");
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void findAllByOrganizationId_ShouldReturnEmptyPage_WhenNoAssetsForOrganization() {
        // Given
        Organization emptyOrg = new Organization();
        emptyOrg.setName("Empty Org");
        entityManager.persistAndFlush(emptyOrg);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Asset> result = assetRepository.findAllByOrganizationId(emptyOrg.getId(), pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findAllByOrganizationId_ShouldRespectPagination() {
        // Given
        Pageable firstPage = PageRequest.of(0, 1);
        Pageable secondPage = PageRequest.of(1, 1);

        // When
        Page<Asset> firstResult = assetRepository.findAllByOrganizationId(organization1.getId(), firstPage);
        Page<Asset> secondResult = assetRepository.findAllByOrganizationId(organization1.getId(), secondPage);

        // Then
        assertThat(firstResult.getContent()).hasSize(1);
        assertThat(secondResult.getContent()).hasSize(1);
        assertThat(firstResult.getTotalElements()).isEqualTo(2);
        assertThat(secondResult.getTotalElements()).isEqualTo(2);
        assertThat(firstResult.getNumber()).isEqualTo(0);
        assertThat(secondResult.getNumber()).isEqualTo(1);
        
        // Ensure different assets are returned
        assertThat(firstResult.getContent().get(0).getId())
                .isNotEqualTo(secondResult.getContent().get(0).getId());
    }

    @Test
    void findAllByOrganizationId_ShouldNotReturnAssetsFromOtherOrganizations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Asset> result = assetRepository.findAllByOrganizationId(organization2.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAssetTag()).isEqualTo("ASSET-003");
        assertThat(result.getContent().get(0).getOrganization().getId()).isEqualTo(organization2.getId());
    }

    @Test
    void save_ShouldPersistAsset() {
        // Given
        Asset newAsset = new Asset();
        newAsset.setAssetTag("NEW-ASSET");
        newAsset.setName("New Asset");
        newAsset.setQrCodeId("QR-NEW");
        newAsset.setOrganization(organization1);
        newAsset.setAssetType(assetType);
        newAsset.setStatus(AssetStatus.ACTIVE);

        // When
        Asset savedAsset = assetRepository.save(newAsset);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedAsset.getId()).isNotNull();
        assertThat(savedAsset.getVersion()).isEqualTo(0);
        assertThat(savedAsset.getCreatedAt()).isNotNull();
        assertThat(savedAsset.getUpdatedAt()).isNotNull();

        // Verify it can be found
        Optional<Asset> foundAsset = assetRepository.findById(savedAsset.getId());
        assertThat(foundAsset).isPresent();
        assertThat(foundAsset.get().getAssetTag()).isEqualTo("NEW-ASSET");
    }

    @Test
    void findById_ShouldReturnAsset_WhenExists() {
        // When
        Optional<Asset> result = assetRepository.findById(asset1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAssetTag()).isEqualTo("ASSET-001");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenDoesNotExist() {
        // When
        Optional<Asset> result = assetRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldRemoveAsset() {
        // Given
        Long assetId = asset1.getId();
        assertThat(assetRepository.findById(assetId)).isPresent();

        // When
        assetRepository.delete(asset1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(assetRepository.findById(assetId)).isEmpty();
    }

    @Test
    void count_ShouldReturnTotalNumberOfAssets() {
        // When
        long count = assetRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }
}