package com.safework.api.domain.organization.integration;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.organization.dto.CreateOrganizationRequest;
import com.safework.api.domain.organization.dto.OrganizationDto;
import com.safework.api.domain.organization.dto.UpdateOrganizationRequest;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.model.OrganizationSize;
import com.safework.api.domain.organization.repository.OrganizationRepository;
import com.safework.api.domain.organization.service.OrganizationService;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class OrganizationIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationRepository organizationRepository;

    private OrganizationService organizationService;

    private Organization organization1;
    private Organization organization2;
    private User adminUser1;
    private User adminUser2;
    private User supervisorUser1;

    @BeforeEach
    void setUp() {
        // Create organization service with real dependencies
        organizationService = new OrganizationService(
                organizationRepository,
                new com.safework.api.domain.organization.mapper.OrganizationMapper()
        );

        // Setup test organizations
        organization1 = new Organization();
        organization1.setName("Tech Solutions Inc");
        organization1.setAddress("123 Innovation Drive, Tech City");
        organization1.setPhone("+1-555-0123");
        organization1.setWebsite("https://techsolutions.com");
        organization1.setIndustry("Technology");
        organization1.setSize(OrganizationSize.MEDIUM);
        entityManager.persistAndFlush(organization1);

        organization2 = new Organization();
        organization2.setName("Manufacturing Corp");
        organization2.setAddress("456 Factory Lane, Industrial Zone");
        organization2.setPhone("+1-555-0456");
        organization2.setWebsite("https://mfgcorp.com");
        organization2.setIndustry("Manufacturing");
        organization2.setSize(OrganizationSize.LARGE);
        entityManager.persistAndFlush(organization2);

        // Setup test users
        adminUser1 = new User();
        adminUser1.setName("Admin User 1");
        adminUser1.setEmail("admin1@techsolutions.com");
        adminUser1.setPassword("hashedPassword1");
        adminUser1.setRole(UserRole.ADMIN);
        adminUser1.setOrganization(organization1);
        entityManager.persistAndFlush(adminUser1);

        adminUser2 = new User();
        adminUser2.setName("Admin User 2");
        adminUser2.setEmail("admin2@mfgcorp.com");
        adminUser2.setPassword("hashedPassword2");
        adminUser2.setRole(UserRole.ADMIN);
        adminUser2.setOrganization(organization2);
        entityManager.persistAndFlush(adminUser2);

        supervisorUser1 = new User();
        supervisorUser1.setName("Supervisor User 1");
        supervisorUser1.setEmail("supervisor1@techsolutions.com");
        supervisorUser1.setPassword("hashedPassword3");
        supervisorUser1.setRole(UserRole.SUPERVISOR);
        supervisorUser1.setOrganization(organization1);
        entityManager.persistAndFlush(supervisorUser1);

        entityManager.clear();
    }

    @Test
    void createOrganization_ShouldCreateWithAllFields_AndPersistToDatabase() {
        // Given
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "Healthcare Plus",
                "789 Medical Center Blvd",
                "+1-555-0789",
                "https://healthcareplus.com",
                "Healthcare",
                "ENTERPRISE"
        );

        // When
        OrganizationDto result = organizationService.createOrganization(request, adminUser1);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Healthcare Plus");
        assertThat(result.address()).isEqualTo("789 Medical Center Blvd");
        assertThat(result.phone()).isEqualTo("+1-555-0789");
        assertThat(result.website()).isEqualTo("https://healthcareplus.com");
        assertThat(result.industry()).isEqualTo("Healthcare");
        assertThat(result.size()).isEqualTo("ENTERPRISE");
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();

        // Verify it was persisted
        Organization saved = organizationRepository.findById(result.id()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Healthcare Plus");
        assertThat(saved.getSize()).isEqualTo(OrganizationSize.ENTERPRISE);
    }

    @Test
    void createOrganization_ShouldCreateWithMinimalFields() {
        // Given
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "Minimal Corp", null, null, null, null, null
        );

        // When
        OrganizationDto result = organizationService.createOrganization(request, adminUser1);

        // Then
        assertThat(result.name()).isEqualTo("Minimal Corp");
        assertThat(result.address()).isNull();
        assertThat(result.phone()).isNull();
        assertThat(result.website()).isNull();
        assertThat(result.industry()).isNull();
        assertThat(result.size()).isNull();

        // Verify persistence
        Organization saved = organizationRepository.findById(result.id()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getSize()).isNull();
    }

    @Test
    void createOrganization_ShouldThrowConflictException_WhenNameExists() {
        // Given
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "Tech Solutions Inc", // Same name as organization1
                "Different Address",
                "+1-555-9999",
                "https://different.com",
                "Software",
                "SMALL"
        );

        // When/Then
        assertThatThrownBy(() -> organizationService.createOrganization(request, adminUser1))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Organization with name 'Tech Solutions Inc' already exists");
    }

    @Test
    void findAllOrganizations_ShouldReturnAllOrganizationsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<com.safework.api.domain.organization.dto.OrganizationSummaryDto> result = 
                organizationService.findAllOrganizations(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting("name")
                .containsExactlyInAnyOrder("Tech Solutions Inc", "Manufacturing Corp");
        assertThat(result.getContent()).extracting("industry")
                .containsExactlyInAnyOrder("Technology", "Manufacturing");
        assertThat(result.getContent()).extracting("size")
                .containsExactlyInAnyOrder("MEDIUM", "LARGE");
    }

    @Test
    void findOrganizationById_ShouldReturnCompleteOrganizationDetails() {
        // When
        OrganizationDto result = organizationService.findOrganizationById(organization1.getId());

        // Then
        assertThat(result.id()).isEqualTo(organization1.getId());
        assertThat(result.name()).isEqualTo("Tech Solutions Inc");
        assertThat(result.address()).isEqualTo("123 Innovation Drive, Tech City");
        assertThat(result.phone()).isEqualTo("+1-555-0123");
        assertThat(result.website()).isEqualTo("https://techsolutions.com");
        assertThat(result.industry()).isEqualTo("Technology");
        assertThat(result.size()).isEqualTo("MEDIUM");
    }

    @Test
    void findOrganizationById_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> organizationService.findOrganizationById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Organization not found with id: 999");
    }

    @Test
    void getCurrentUserOrganization_ShouldReturnUserOrganization() {
        // When
        OrganizationDto result = organizationService.getCurrentUserOrganization(adminUser1);

        // Then
        assertThat(result.id()).isEqualTo(organization1.getId());
        assertThat(result.name()).isEqualTo("Tech Solutions Inc");
    }

    @Test
    void updateOrganization_ShouldUpdateAllFields_AndPersistChanges() {
        // Given
        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "Updated Tech Solutions",
                "999 New Innovation Park",
                "+1-555-9999",
                "https://updatedtech.com",
                "Software Development",
                "ENTERPRISE"
        );

        // When
        OrganizationDto result = organizationService.updateOrganization(
                organization1.getId(), request, adminUser1
        );

        // Then
        assertThat(result.name()).isEqualTo("Updated Tech Solutions");
        assertThat(result.address()).isEqualTo("999 New Innovation Park");
        assertThat(result.phone()).isEqualTo("+1-555-9999");
        assertThat(result.website()).isEqualTo("https://updatedtech.com");
        assertThat(result.industry()).isEqualTo("Software Development");
        assertThat(result.size()).isEqualTo("ENTERPRISE");

        // Verify persistence
        entityManager.flush();
        entityManager.clear();
        Organization updated = organizationRepository.findById(organization1.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Tech Solutions");
        assertThat(updated.getSize()).isEqualTo(OrganizationSize.ENTERPRISE);
    }

    @Test
    void updateOrganization_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // Given
        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "Hacked Organization", "Evil Address", "+1-555-EVIL",
                "https://hacked.com", "Hacking", "ENTERPRISE"
        );

        // When/Then - User from organization2 trying to update organization1
        assertThatThrownBy(() -> organizationService.updateOrganization(
                organization1.getId(), request, adminUser2
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this organization");
    }

    @Test
    void updateOrganization_ShouldThrowAccessDeniedException_WhenUserIsNotAdmin() {
        // Given
        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "Unauthorized Update", "Supervisor Address", "+1-555-SUPER",
                "https://supervisor.com", "Supervision", "LARGE"
        );

        // When/Then - Supervisor trying to update organization
        assertThatThrownBy(() -> organizationService.updateOrganization(
                organization1.getId(), request, supervisorUser1
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only administrators can modify organization details");
    }

    @Test
    void deleteOrganization_ShouldDeleteSuccessfully_WhenOnlyOneUser() {
        // Given - Create organization with single user
        Organization singleUserOrg = new Organization();
        singleUserOrg.setName("Single User Org");
        entityManager.persistAndFlush(singleUserOrg);

        User singleUser = new User();
        singleUser.setName("Only User");
        singleUser.setEmail("only@singleuser.com");
        singleUser.setPassword("hashedPassword");
        singleUser.setRole(UserRole.ADMIN);
        singleUser.setOrganization(singleUserOrg);
        entityManager.persistAndFlush(singleUser);

        entityManager.flush();
        entityManager.clear();

        // Refresh entities to get proper relationships
        singleUserOrg = organizationRepository.findById(singleUserOrg.getId()).orElse(null);
        singleUser = entityManager.find(User.class, singleUser.getId());

        // When
        organizationService.deleteOrganization(singleUserOrg.getId(), singleUser);

        // Then
        entityManager.flush();
        entityManager.clear();
        assertThat(organizationRepository.findById(singleUserOrg.getId())).isEmpty();
    }

    @Test
    void deleteOrganization_ShouldThrowConflictException_WhenMultipleUsers() {
        // Given - organization1 has both adminUser1 and supervisorUser1
        entityManager.flush();
        entityManager.clear();
        organization1 = organizationRepository.findById(organization1.getId()).orElse(null);
        adminUser1 = entityManager.find(User.class, adminUser1.getId());

        // When/Then
        assertThatThrownBy(() -> organizationService.deleteOrganization(
                organization1.getId(), adminUser1
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Cannot delete organization with multiple users. Please remove all other users first.");
    }

    @Test
    void organizationLifecycle_ShouldMaintainRelationshipsWithUsersAndAssets() {
        // Given - Create organization with users and assets
        Organization testOrg = new Organization();
        testOrg.setName("Test Lifecycle Org");
        testOrg.setSize(OrganizationSize.SMALL);
        entityManager.persistAndFlush(testOrg);

        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@lifecycle.com");
        user1.setPassword("hashedPassword");
        user1.setRole(UserRole.ADMIN);
        user1.setOrganization(testOrg);
        entityManager.persistAndFlush(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@lifecycle.com");
        user2.setPassword("hashedPassword");
        user2.setRole(UserRole.SUPERVISOR);
        user2.setOrganization(testOrg);
        entityManager.persistAndFlush(user2);

        AssetType assetType = new AssetType();
        assetType.setName("Test Equipment");
        assetType.setOrganization(testOrg);
        entityManager.persistAndFlush(assetType);

        Asset asset1 = new Asset();
        asset1.setAssetTag("LIFECYCLE-001");
        asset1.setName("Test Asset 1");
        asset1.setQrCodeId("QR-LIFECYCLE-001");
        asset1.setOrganization(testOrg);
        asset1.setAssetType(assetType);
        asset1.setStatus(AssetStatus.ACTIVE);
        entityManager.persistAndFlush(asset1);

        Asset asset2 = new Asset();
        asset2.setAssetTag("LIFECYCLE-002");
        asset2.setName("Test Asset 2");
        asset2.setQrCodeId("QR-LIFECYCLE-002");
        asset2.setOrganization(testOrg);
        asset2.setAssetType(assetType);
        asset2.setStatus(AssetStatus.INACTIVE);
        entityManager.persistAndFlush(asset2);

        entityManager.flush();
        entityManager.clear();

        // When - Retrieve organization and verify relationships
        OrganizationDto result = organizationService.findOrganizationById(testOrg.getId());
        Organization reloaded = organizationRepository.findById(testOrg.getId()).orElse(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Lifecycle Org");
        assertThat(result.size()).isEqualTo("SMALL");

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getUsers()).hasSize(2);
        assertThat(reloaded.getUsers()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@lifecycle.com", "user2@lifecycle.com");
        assertThat(reloaded.getAssets()).hasSize(2);
        assertThat(reloaded.getAssets()).extracting(Asset::getAssetTag)
                .containsExactlyInAnyOrder("LIFECYCLE-001", "LIFECYCLE-002");
    }

    @Test
    void multiTenantSecurity_ShouldEnforceOrganizationBoundaries() {
        // Given - Two users from different organizations
        UpdateOrganizationRequest updateRequest = new UpdateOrganizationRequest(
                "Cross-Tenant Attack", "Malicious Address", "+1-555-EVIL",
                "https://evil.com", "Hacking", "ENTERPRISE"
        );

        // When/Then - findOrganizationById is system-level, no tenant check at service level
        OrganizationDto result = organizationService.findOrganizationById(organization1.getId());
        assertThat(result).isNotNull(); // Service level doesn't enforce tenant boundaries for system operations

        // But update should fail due to organization mismatch
        assertThatThrownBy(() -> organizationService.updateOrganization(
                organization1.getId(), updateRequest, adminUser2
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this organization");

        // And delete should also fail
        assertThatThrownBy(() -> organizationService.deleteOrganization(
                organization1.getId(), adminUser2
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this organization");
    }
}