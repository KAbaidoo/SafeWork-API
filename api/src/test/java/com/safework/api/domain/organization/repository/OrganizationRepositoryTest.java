package com.safework.api.domain.organization.repository;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.organization.model.OrganizationSize;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization testOrg1;
    private Organization testOrg2;

    @BeforeEach
    void setUp() {
        testOrg1 = new Organization();
        testOrg1.setName("Test Organization 1");
        testOrg1.setAddress("123 Test Street, Test City");
        testOrg1.setPhone("+1-555-0123");
        testOrg1.setWebsite("https://testorg1.com");
        testOrg1.setIndustry("Technology");
        testOrg1.setSize(OrganizationSize.MEDIUM);
        entityManager.persistAndFlush(testOrg1);

        testOrg2 = new Organization();
        testOrg2.setName("Test Organization 2");
        testOrg2.setAddress("456 Another Street, Another City");
        testOrg2.setPhone("+1-555-0456");
        testOrg2.setWebsite("https://testorg2.com");
        testOrg2.setIndustry("Manufacturing");
        testOrg2.setSize(OrganizationSize.LARGE);
        entityManager.persistAndFlush(testOrg2);

        entityManager.clear();
    }

    @Test
    void findByName_ShouldReturnOrganization_WhenNameExists() {
        // When
        Optional<Organization> result = organizationRepository.findByName("Test Organization 1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Organization 1");
        assertThat(result.get().getAddress()).isEqualTo("123 Test Street, Test City");
        assertThat(result.get().getPhone()).isEqualTo("+1-555-0123");
        assertThat(result.get().getWebsite()).isEqualTo("https://testorg1.com");
        assertThat(result.get().getIndustry()).isEqualTo("Technology");
        assertThat(result.get().getSize()).isEqualTo(OrganizationSize.MEDIUM);
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // When
        Optional<Organization> result = organizationRepository.findByName("Non-existent Organization");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameIsNull() {
        // When
        Optional<Organization> result = organizationRepository.findByName(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByName_ShouldBeCaseSensitive() {
        // When
        Optional<Organization> result = organizationRepository.findByName("test organization 1");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistOrganization_WithAllFields() {
        // Given
        Organization newOrg = new Organization();
        newOrg.setName("New Test Organization");
        newOrg.setAddress("789 New Street, New City");
        newOrg.setPhone("+1-555-0789");
        newOrg.setWebsite("https://newtestorg.com");
        newOrg.setIndustry("Healthcare");
        newOrg.setSize(OrganizationSize.ENTERPRISE);

        // When
        Organization savedOrg = organizationRepository.save(newOrg);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedOrg.getId()).isNotNull();
        assertThat(savedOrg.getCreatedAt()).isNotNull();
        assertThat(savedOrg.getUpdatedAt()).isNotNull();

        // Verify it can be found
        Optional<Organization> foundOrg = organizationRepository.findById(savedOrg.getId());
        assertThat(foundOrg).isPresent();
        assertThat(foundOrg.get().getName()).isEqualTo("New Test Organization");
        assertThat(foundOrg.get().getAddress()).isEqualTo("789 New Street, New City");
        assertThat(foundOrg.get().getPhone()).isEqualTo("+1-555-0789");
        assertThat(foundOrg.get().getWebsite()).isEqualTo("https://newtestorg.com");
        assertThat(foundOrg.get().getIndustry()).isEqualTo("Healthcare");
        assertThat(foundOrg.get().getSize()).isEqualTo(OrganizationSize.ENTERPRISE);
    }

    @Test
    void save_ShouldPersistOrganization_WithOnlyRequiredFields() {
        // Given
        Organization minimalOrg = new Organization();
        minimalOrg.setName("Minimal Organization");

        // When
        Organization savedOrg = organizationRepository.save(minimalOrg);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Organization> foundOrg = organizationRepository.findById(savedOrg.getId());
        assertThat(foundOrg).isPresent();
        assertThat(foundOrg.get().getName()).isEqualTo("Minimal Organization");
        assertThat(foundOrg.get().getAddress()).isNull();
        assertThat(foundOrg.get().getPhone()).isNull();
        assertThat(foundOrg.get().getWebsite()).isNull();
        assertThat(foundOrg.get().getIndustry()).isNull();
        assertThat(foundOrg.get().getSize()).isNull();
    }

    @Test
    void save_ShouldThrowException_WhenNameIsNull() {
        // Given
        Organization invalidOrg = new Organization();

        // When/Then
        assertThatThrownBy(() -> {
            organizationRepository.save(invalidOrg);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldThrowException_WhenNameAlreadyExists() {
        // Given
        Organization duplicateOrg = new Organization();
        duplicateOrg.setName("Test Organization 1"); // Same name as testOrg1

        // When/Then
        assertThatThrownBy(() -> {
            organizationRepository.save(duplicateOrg);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAll_ShouldReturnAllOrganizations() {
        // When
        List<Organization> organizations = organizationRepository.findAll();

        // Then
        assertThat(organizations).hasSize(2);
        assertThat(organizations).extracting(Organization::getName)
                .containsExactlyInAnyOrder("Test Organization 1", "Test Organization 2");
    }

    @Test
    void findById_ShouldReturnOrganization_WhenExists() {
        // When
        Optional<Organization> result = organizationRepository.findById(testOrg1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Organization 1");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenDoesNotExist() {
        // When
        Optional<Organization> result = organizationRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldRemoveOrganization() {
        // Given
        Long orgId = testOrg1.getId();
        assertThat(organizationRepository.findById(orgId)).isPresent();

        // When
        organizationRepository.delete(testOrg1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(organizationRepository.findById(orgId)).isEmpty();
    }

    @Test
    void organizationWithUsers_ShouldMaintainRelationships() {
        // Given
        User user1 = new User();
        user1.setEmail("user1@testorg1.com");
        user1.setName("User One");
        user1.setPassword("hashedPassword1");
        user1.setRole(UserRole.ADMIN);
        user1.setOrganization(testOrg1);

        User user2 = new User();
        user2.setEmail("user2@testorg1.com");
        user2.setName("User Two");
        user2.setPassword("hashedPassword2");
        user2.setRole(UserRole.SUPERVISOR);
        user2.setOrganization(testOrg1);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        entityManager.clear();

        // When
        Organization loadedOrg = organizationRepository.findById(testOrg1.getId()).get();

        // Then
        assertThat(loadedOrg.getUsers()).hasSize(2);
        assertThat(loadedOrg.getUsers()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@testorg1.com", "user2@testorg1.com");
    }

    @Test
    void organizationWithAssets_ShouldMaintainRelationships() {
        // Given
        AssetType assetType = new AssetType();
        assetType.setName("Test Equipment");
        assetType.setOrganization(testOrg1);
        entityManager.persist(assetType);

        Asset asset1 = new Asset();
        asset1.setAssetTag("ORG1-001");
        asset1.setName("Asset One");
        asset1.setQrCodeId("QR-ORG1-001");
        asset1.setOrganization(testOrg1);
        asset1.setAssetType(assetType);
        asset1.setStatus(AssetStatus.ACTIVE);

        Asset asset2 = new Asset();
        asset2.setAssetTag("ORG1-002");
        asset2.setName("Asset Two");
        asset2.setQrCodeId("QR-ORG1-002");
        asset2.setOrganization(testOrg1);
        asset2.setAssetType(assetType);
        asset2.setStatus(AssetStatus.INACTIVE);

        entityManager.persist(asset1);
        entityManager.persist(asset2);
        entityManager.flush();
        entityManager.clear();

        // When
        Organization loadedOrg = organizationRepository.findById(testOrg1.getId()).get();

        // Then
        assertThat(loadedOrg.getAssets()).hasSize(2);
        assertThat(loadedOrg.getAssets()).extracting(Asset::getAssetTag)
                .containsExactlyInAnyOrder("ORG1-001", "ORG1-002");
    }

    @Test
    void organizationSizeEnum_ShouldPersistCorrectly() {
        // Given
        Organization smallOrg = new Organization();
        smallOrg.setName("Small Organization");
        smallOrg.setSize(OrganizationSize.SMALL);

        Organization enterpriseOrg = new Organization();
        enterpriseOrg.setName("Enterprise Organization");
        enterpriseOrg.setSize(OrganizationSize.ENTERPRISE);

        // When
        organizationRepository.save(smallOrg);
        organizationRepository.save(enterpriseOrg);
        entityManager.flush();
        entityManager.clear();

        // Then
        Organization loadedSmall = organizationRepository.findByName("Small Organization").get();
        Organization loadedEnterprise = organizationRepository.findByName("Enterprise Organization").get();

        assertThat(loadedSmall.getSize()).isEqualTo(OrganizationSize.SMALL);
        assertThat(loadedEnterprise.getSize()).isEqualTo(OrganizationSize.ENTERPRISE);
    }

    @Test
    void timestampAuditing_ShouldWorkCorrectly() throws InterruptedException {
        // Given
        Organization org = new Organization();
        org.setName("Timestamp Test Organization");

        // When
        Organization savedOrg = organizationRepository.save(org);
        entityManager.flush();

        assertThat(savedOrg.getCreatedAt()).isNotNull();
        assertThat(savedOrg.getUpdatedAt()).isNotNull();
        
        // Initial creation timestamps should be close
        assertThat(savedOrg.getCreatedAt()).isEqualToIgnoringNanos(savedOrg.getUpdatedAt());

        Thread.sleep(100); // Ensure time difference
        savedOrg.setName("Updated Timestamp Test");
        Organization updatedOrg = organizationRepository.save(savedOrg);
        entityManager.flush();

        // Then
        assertThat(updatedOrg.getCreatedAt()).isEqualTo(savedOrg.getCreatedAt());
        assertThat(updatedOrg.getUpdatedAt()).isAfter(updatedOrg.getCreatedAt());
    }

    @Test
    void count_ShouldReturnCorrectNumber() {
        // When
        long count = organizationRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }
}