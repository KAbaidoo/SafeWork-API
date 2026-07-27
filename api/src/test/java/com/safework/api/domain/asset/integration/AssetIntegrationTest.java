package com.safework.api.domain.asset.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.asset.dto.CreateAssetRequest;
import com.safework.api.domain.asset.dto.UpdateAssetRequest;
import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.asset.repository.AssetTypeRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.repository.OrganizationRepository;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.security.PrincipalUser;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AssetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private Organization organization;
    private User adminUser;
    private User inspectorUser;
    private AssetType assetType;
    private Asset asset;

    @BeforeEach
    void setUp() {
        // Clean up
        assetRepository.deleteAll();
        assetTypeRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // Create organization
        organization = new Organization();
        organization.setName("Integration Test Organization");
        organization = organizationRepository.save(organization);

        // Create users
        adminUser = new User();
        adminUser.setEmail("admin@integration.test");
        adminUser.setName("Admin User");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);
        adminUser = userRepository.save(adminUser);

        inspectorUser = new User();
        inspectorUser.setEmail("inspector@integration.test");
        inspectorUser.setName("Inspector User");
        inspectorUser.setPassword(passwordEncoder.encode("password"));
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(organization);
        inspectorUser = userRepository.save(inspectorUser);

        // Create asset type
        assetType = new AssetType();
        assetType.setName("Integration Test Equipment");
        assetType.setOrganization(organization);
        assetType = assetTypeRepository.save(assetType);

        // Create test asset
        asset = new Asset();
        asset.setAssetTag("INTEGRATION-001");
        asset.setName("Integration Test Asset");
        asset.setQrCodeId("QR-INTEGRATION-001");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);
        asset = assetRepository.save(asset);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void createAsset_ShouldCreateAssetInDatabase_WhenValidRequest() throws Exception {
        // Given
        CreateAssetRequest request = new CreateAssetRequest(
            "NEW-ASSET-001",
            "New Integration Asset",
            "QR-NEW-001",
            assetType.getId()
        );

        // When
        mockMvc.perform(post("/v1/assets")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.assetTag").value("NEW-ASSET-001"))
            .andExpect(jsonPath("$.name").value("New Integration Asset"))
            .andExpect(jsonPath("$.status").value("INACTIVE")); // Default status

        // Then - verify in database
        Optional<Asset> createdAsset = assetRepository.findByQrCodeId("QR-NEW-001");
        assertThat(createdAsset).isPresent();
        assertThat(createdAsset.get().getAssetTag()).isEqualTo("NEW-ASSET-001");
        assertThat(createdAsset.get().getName()).isEqualTo("New Integration Asset");
        assertThat(createdAsset.get().getStatus()).isEqualTo(AssetStatus.INACTIVE);
        assertThat(createdAsset.get().getOrganization().getId()).isEqualTo(organization.getId());
        assertThat(createdAsset.get().getAssetType().getId()).isEqualTo(assetType.getId());
    }

    @Test
    void getAssetsByOrganization_ShouldReturnAssetsFromDatabase() throws Exception {
        // When/Then
        mockMvc.perform(get("/v1/assets")
                .with(user(new PrincipalUser(adminUser)))
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].assetTag").value("INTEGRATION-001"))
            .andExpect(jsonPath("$.content[0].name").value("Integration Test Asset"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAssetById_ShouldReturnAssetFromDatabase() throws Exception {
        // When/Then
        mockMvc.perform(get("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(adminUser))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(asset.getId()))
            .andExpect(jsonPath("$.assetTag").value("INTEGRATION-001"))
            .andExpect(jsonPath("$.name").value("Integration Test Asset"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getAssetByQrCode_ShouldReturnAssetFromDatabase() throws Exception {
        // When/Then
        mockMvc.perform(get("/v1/assets/qr/QR-INTEGRATION-001")
                .with(user(new PrincipalUser(adminUser))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assetTag").value("INTEGRATION-001"))
            .andExpect(jsonPath("$.name").value("Integration Test Asset"));
    }

    @Test
    void updateAsset_ShouldUpdateAssetInDatabase_WhenValidRequest() throws Exception {
        // Given
        UpdateAssetRequest request = new UpdateAssetRequest(
            "Updated Integration Asset",
            null,
            "INACTIVE",
            asset.getVersion() // Current version for optimistic locking
        );

        // When
        mockMvc.perform(put("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Integration Asset"))
            .andExpect(jsonPath("$.status").value("INACTIVE"))
            .andExpect(jsonPath("$.version").value(asset.getVersion() + 1));

        // Then - verify in database
        entityManager.flush();
        entityManager.clear();
        Optional<Asset> updatedAsset = assetRepository.findById(asset.getId());
        assertThat(updatedAsset).isPresent();
        assertThat(updatedAsset.get().getName()).isEqualTo("Updated Integration Asset");
        assertThat(updatedAsset.get().getStatus()).isEqualTo(AssetStatus.INACTIVE);
        assertThat(updatedAsset.get().getVersion()).isEqualTo(asset.getVersion() + 1);
    }

    @Test
    void updateAsset_ShouldReturnConflict_WhenVersionMismatch() throws Exception {
        // Given - simulate concurrent update by incrementing version
        asset.setName("Concurrently updated");
        assetRepository.save(asset);
        entityManager.flush();

        UpdateAssetRequest request = new UpdateAssetRequest(
            "Another Update",
            null,
            "UNDER_MAINTENANCE",
            0 // Old version - should cause conflict
        );

        // When/Then
        mockMvc.perform(put("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void deleteAsset_ShouldRemoveAssetFromDatabase() throws Exception {
        // Given
        Long assetId = asset.getId();
        assertThat(assetRepository.findById(assetId)).isPresent();

        // When
        mockMvc.perform(delete("/v1/assets/" + assetId)
                .with(user(new PrincipalUser(adminUser))))
            .andExpect(status().isNoContent());

        // Then - verify asset is deleted from database
        assertThat(assetRepository.findById(assetId)).isEmpty();
    }

    @Test
    void multiTenantSecurity_ShouldPreventAccessToOtherOrganizationAssets() throws Exception {
        // Given - create another organization and user
        Organization otherOrg = new Organization();
        otherOrg.setName("Other Organization");
        otherOrg = organizationRepository.save(otherOrg);

        User otherUser = new User();
        otherUser.setEmail("other@test.com");
        otherUser.setName("Other User");
        otherUser.setPassword(passwordEncoder.encode("password"));
        otherUser.setRole(UserRole.ADMIN);
        otherUser.setOrganization(otherOrg);
        otherUser = userRepository.save(otherUser);

        // When/Then - other user should not be able to access assets from different org
        mockMvc.perform(get("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(otherUser))))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/v1/assets/qr/QR-INTEGRATION-001")
                .with(user(new PrincipalUser(otherUser))))
            .andExpect(status().isForbidden());
    }

    @Test
    void roleBasedSecurity_ShouldEnforcePermissions() throws Exception {
        // Given
        CreateAssetRequest createRequest = new CreateAssetRequest(
            "INSPECTOR-ASSET",
            "Inspector Asset",
            "QR-INSPECTOR",
            assetType.getId()
        );

        UpdateAssetRequest updateRequest = new UpdateAssetRequest(
            "Updated by Inspector",
            null,
            "INACTIVE",
            asset.getVersion()
        );

        // When/Then - Inspector should not be able to create assets
        mockMvc.perform(post("/v1/assets")
                .with(user(new PrincipalUser(inspectorUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());

        // Inspector should not be able to update assets
        mockMvc.perform(put("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(inspectorUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());

        // Inspector should not be able to delete assets
        mockMvc.perform(delete("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(inspectorUser))))
            .andExpect(status().isForbidden());

        // But Inspector should be able to view assets
        mockMvc.perform(get("/v1/assets")
                .with(user(new PrincipalUser(inspectorUser))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(inspectorUser))))
            .andExpect(status().isOk());
    }

    @Test
    void transactionBoundaries_ShouldRollbackOnFailure() throws Exception {
        // Given
        long initialAssetCount = assetRepository.count();

        CreateAssetRequest request = new CreateAssetRequest(
            "INVALID-ASSET",
            "Invalid Asset",
            "QR-INVALID",
            999L // Non-existent asset type ID
        );

        // When
        mockMvc.perform(post("/v1/assets")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());

        // Then - no new asset should be created due to rollback
        assertThat(assetRepository.count()).isEqualTo(initialAssetCount);
    }

    @Test
    void optimisticLocking_ShouldPreventConcurrentUpdates() throws Exception {
        // Given
        UpdateAssetRequest request1 = new UpdateAssetRequest(
            "First Update",
            null,
            "INACTIVE",
            asset.getVersion()
        );

        UpdateAssetRequest request2 = new UpdateAssetRequest(
            "Second Update",
            null,
            "UNDER_MAINTENANCE",
            asset.getVersion() // Same version - should fail
        );

        // When - first update should succeed
        mockMvc.perform(put("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(asset.getVersion() + 1));

        // Then - second update with same version should fail
        mockMvc.perform(put("/v1/assets/" + asset.getId())
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isConflict());
    }
}