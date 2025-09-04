package com.safework.api.domain.organization;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class OrganizationRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void testCreateAndRetrieveOrganization() {
        Organization org = new Organization();
        org.setName("Test Corp");

        entityManager.persist(org);
        entityManager.flush();
        entityManager.clear();

        Organization loaded = entityManager.find(Organization.class, org.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getName()).isEqualTo("Test Corp");
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getId()).isNotNull();
    }

    @Test
    void testOrganizationNameUniqueConstraint() {
        Organization org1 = new Organization();
        org1.setName("Unique Corp");
        entityManager.persist(org1);
        entityManager.flush();

        Organization org2 = new Organization();
        org2.setName("Unique Corp");

        assertThatThrownBy(() -> {
            entityManager.persist(org2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void testOrganizationWithUsers() {
        Organization org = new Organization();
        org.setName("User Test Corp");
        entityManager.persist(org);

        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setName("User One");
        user1.setPassword("hashed1");
        user1.setRole(UserRole.ADMIN);
        user1.setOrganization(org);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setName("User Two");
        user2.setPassword("hashed2");
        user2.setRole(UserRole.INSPECTOR);
        user2.setOrganization(org);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        entityManager.clear();

        Organization loaded = entityManager.find(Organization.class, org.getId());
        List<User> users = loaded.getUsers();
        
        assertThat(users).isNotNull();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
            .containsExactlyInAnyOrder("user1@test.com", "user2@test.com");
    }

    @Test
    void testOrganizationWithAssets() {
        Organization org = new Organization();
        org.setName("Asset Test Corp");
        entityManager.persist(org);

        AssetType assetType = new AssetType();
        assetType.setName("Equipment");
        assetType.setOrganization(org);
        entityManager.persist(assetType);

        Asset asset1 = new Asset();
        asset1.setAssetTag("ASSET-001");
        asset1.setName("Test Equipment 1");
        asset1.setOrganization(org);
        asset1.setAssetType(assetType);
        asset1.setStatus(AssetStatus.ACTIVE);

        Asset asset2 = new Asset();
        asset2.setAssetTag("ASSET-002");
        asset2.setName("Test Equipment 2");
        asset2.setOrganization(org);
        asset2.setAssetType(assetType);
        asset2.setStatus(AssetStatus.INACTIVE);

        entityManager.persist(asset1);
        entityManager.persist(asset2);
        entityManager.flush();
        entityManager.clear();

        Organization loaded = entityManager.find(Organization.class, org.getId());
        List<Asset> assets = loaded.getAssets();
        
        assertThat(assets).isNotNull();
        assertThat(assets).hasSize(2);
        assertThat(assets).extracting(Asset::getAssetTag)
            .containsExactlyInAnyOrder("ASSET-001", "ASSET-002");
    }

    @Test
    void testOrganizationCascadeDeleteWithUsers() {
        Organization org = new Organization();
        org.setName("Cascade Test Corp");
        entityManager.persist(org);

        User user = new User();
        user.setEmail("cascade@test.com");
        user.setName("Cascade User");
        user.setPassword("hashed");
        user.setRole(UserRole.ADMIN);
        user.setOrganization(org);
        entityManager.persist(user);
        
        entityManager.flush();
        Long userId = user.getId();
        Long orgId = org.getId();
        
        entityManager.clear();

        Organization toDelete = entityManager.find(Organization.class, orgId);
        entityManager.remove(toDelete);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Organization.class, orgId)).isNull();
        assertThat(entityManager.find(User.class, userId)).isNull();
    }

    @Test
    void testOrganizationLazyLoadingCollections() {
        Organization org = new Organization();
        org.setName("Lazy Load Test Corp");
        entityManager.persist(org);

        User user = new User();
        user.setEmail("lazy@test.com");
        user.setName("Lazy User");
        user.setPassword("hashed");
        user.setRole(UserRole.ADMIN);
        user.setOrganization(org);
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        Organization loaded = entityManager.find(Organization.class, org.getId());
        
        assertThat(loaded).isNotNull();
        assertThat(loaded.getUsers()).isNotNull();
        assertThat(loaded.getUsers()).hasSize(1);
    }

    @Test
    void testOrganizationUpdateName() {
        Organization org = new Organization();
        org.setName("Original Name");
        entityManager.persist(org);
        entityManager.flush();
        entityManager.clear();

        Organization toUpdate = entityManager.find(Organization.class, org.getId());
        toUpdate.setName("Updated Name");
        entityManager.flush();
        entityManager.clear();

        Organization updated = entityManager.find(Organization.class, org.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testOrganizationNameNotNull() {
        Organization org = new Organization();
        
        assertThatThrownBy(() -> {
            entityManager.persist(org);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void testMultipleOrganizationsQuery() {
        Organization org1 = new Organization();
        org1.setName("Org Alpha");
        
        Organization org2 = new Organization();
        org2.setName("Org Beta");
        
        Organization org3 = new Organization();
        org3.setName("Org Gamma");

        entityManager.persist(org1);
        entityManager.persist(org2);
        entityManager.persist(org3);
        entityManager.flush();
        entityManager.clear();

        List<Organization> orgs = entityManager
            .createQuery("SELECT o FROM Organization o ORDER BY o.name", Organization.class)
            .getResultList();

        assertThat(orgs).hasSize(3);
        assertThat(orgs).extracting(Organization::getName)
            .containsExactly("Org Alpha", "Org Beta", "Org Gamma");
    }
}