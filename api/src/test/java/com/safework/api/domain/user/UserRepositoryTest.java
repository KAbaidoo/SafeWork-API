package com.safework.api.domain.user;

import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setName("Test Organization");
        entityManager.persist(organization);
        entityManager.flush();
    }

    @Test
    void testCreateAndRetrieveUser() {
        User user = new User();
        user.setEmail("john@example.com");
        user.setName("John Doe");
        user.setPassword("hashedPassword123");
        user.setRole(UserRole.INSPECTOR);
        user.setOrganization(organization);

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User loaded = entityManager.find(User.class, user.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getEmail()).isEqualTo("john@example.com");
        assertThat(loaded.getName()).isEqualTo("John Doe");
        assertThat(loaded.getPassword()).isEqualTo("hashedPassword123");
        assertThat(loaded.getRole()).isEqualTo(UserRole.INSPECTOR);
        assertThat(loaded.getOrganization().getId()).isEqualTo(organization.getId());
    }

    @Test
    void testUniqueEmailConstraint() {
        User user1 = new User();
        user1.setEmail("duplicate@example.com");
        user1.setName("User One");
        user1.setPassword("password1");
        user1.setRole(UserRole.ADMIN);
        user1.setOrganization(organization);
        entityManager.persist(user1);
        entityManager.flush();

        User user2 = new User();
        user2.setEmail("duplicate@example.com");
        user2.setName("User Two");
        user2.setPassword("password2");
        user2.setRole(UserRole.INSPECTOR);
        user2.setOrganization(organization);

        assertThatThrownBy(() -> {
            entityManager.persist(user2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void testUserWithOptionalDepartment() {
        Department department = new Department();
        department.setName("Engineering");
        department.setOrganization(organization);
        entityManager.persist(department);

        User userWithDept = new User();
        userWithDept.setEmail("with.dept@example.com");
        userWithDept.setName("User With Dept");
        userWithDept.setPassword("password");
        userWithDept.setRole(UserRole.SUPERVISOR);
        userWithDept.setOrganization(organization);
        userWithDept.setDepartment(department);

        User userWithoutDept = new User();
        userWithoutDept.setEmail("without.dept@example.com");
        userWithoutDept.setName("User Without Dept");
        userWithoutDept.setPassword("password");
        userWithoutDept.setRole(UserRole.ADMIN);
        userWithoutDept.setOrganization(organization);

        entityManager.persist(userWithDept);
        entityManager.persist(userWithoutDept);
        entityManager.flush();
        entityManager.clear();

        User loadedWithDept = entityManager.find(User.class, userWithDept.getId());
        User loadedWithoutDept = entityManager.find(User.class, userWithoutDept.getId());

        assertThat(loadedWithDept.getDepartment()).isNotNull();
        assertThat(loadedWithDept.getDepartment().getName()).isEqualTo("Engineering");
        assertThat(loadedWithoutDept.getDepartment()).isNull();
    }

    @Test
    void testUserRoleEnumPersistence() {
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setName("Admin User");
        adminUser.setPassword("password");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);

        User supervisorUser = new User();
        supervisorUser.setEmail("supervisor@example.com");
        supervisorUser.setName("Supervisor User");
        supervisorUser.setPassword("password");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(organization);

        User inspectorUser = new User();
        inspectorUser.setEmail("inspector@example.com");
        inspectorUser.setName("Inspector User");
        inspectorUser.setPassword("password");
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(organization);

        entityManager.persist(adminUser);
        entityManager.persist(supervisorUser);
        entityManager.persist(inspectorUser);
        entityManager.flush();
        entityManager.clear();

        User loadedAdmin = entityManager.find(User.class, adminUser.getId());
        User loadedSupervisor = entityManager.find(User.class, supervisorUser.getId());
        User loadedInspector = entityManager.find(User.class, inspectorUser.getId());

        assertThat(loadedAdmin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(loadedSupervisor.getRole()).isEqualTo(UserRole.SUPERVISOR);
        assertThat(loadedInspector.getRole()).isEqualTo(UserRole.INSPECTOR);
    }

    @Test
    void testTimestampAuditing() throws InterruptedException {
        LocalDateTime beforeCreate = LocalDateTime.now();
        Thread.sleep(10);

        User user = new User();
        user.setEmail("timestamp@example.com");
        user.setName("Timestamp User");
        user.setPassword("password");
        user.setRole(UserRole.ADMIN);
        user.setOrganization(organization);

        entityManager.persist(user);
        entityManager.flush();

        Thread.sleep(10);
        LocalDateTime afterCreate = LocalDateTime.now();

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isAfter(beforeCreate);
        assertThat(user.getCreatedAt()).isBefore(afterCreate);
        // Initial creation timestamps should be equal
        LocalDateTime initialCreated = user.getCreatedAt();
        LocalDateTime initialUpdated = user.getUpdatedAt();
        
        Thread.sleep(100); // Give enough time for timestamp difference
        user.setName("Updated Name");
        entityManager.flush();
        entityManager.clear();
        
        User reloaded = entityManager.find(User.class, user.getId());
        assertThat(reloaded.getCreatedAt()).isEqualTo(initialCreated);
        assertThat(reloaded.getUpdatedAt()).isAfter(initialCreated);
    }

    @Test
    void testUserRequiredFields() {
        User userMissingEmail = new User();
        userMissingEmail.setName("No Email");
        userMissingEmail.setPassword("password");
        userMissingEmail.setRole(UserRole.ADMIN);
        userMissingEmail.setOrganization(organization);

        assertThatThrownBy(() -> {
            entityManager.persist(userMissingEmail);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        User userMissingName = new User();
        userMissingName.setEmail("noname@example.com");
        userMissingName.setPassword("password");
        userMissingName.setRole(UserRole.ADMIN);
        userMissingName.setOrganization(organization);

        assertThatThrownBy(() -> {
            entityManager.persist(userMissingName);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        User userMissingPassword = new User();
        userMissingPassword.setEmail("nopass@example.com");
        userMissingPassword.setName("No Password");
        userMissingPassword.setRole(UserRole.ADMIN);
        userMissingPassword.setOrganization(organization);

        assertThatThrownBy(() -> {
            entityManager.persist(userMissingPassword);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        User userMissingRole = new User();
        userMissingRole.setEmail("norole@example.com");
        userMissingRole.setName("No Role");
        userMissingRole.setPassword("password");
        userMissingRole.setOrganization(organization);

        assertThatThrownBy(() -> {
            entityManager.persist(userMissingRole);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        User userMissingOrg = new User();
        userMissingOrg.setEmail("noorg@example.com");
        userMissingOrg.setName("No Org");
        userMissingOrg.setPassword("password");
        userMissingOrg.setRole(UserRole.ADMIN);

        assertThatThrownBy(() -> {
            entityManager.persist(userMissingOrg);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void testFindUsersByOrganization() {
        Organization org2 = new Organization();
        org2.setName("Second Organization");
        entityManager.persist(org2);

        User user1 = new User();
        user1.setEmail("user1@org1.com");
        user1.setName("Org1 User1");
        user1.setPassword("password");
        user1.setRole(UserRole.ADMIN);
        user1.setOrganization(organization);

        User user2 = new User();
        user2.setEmail("user2@org1.com");
        user2.setName("Org1 User2");
        user2.setPassword("password");
        user2.setRole(UserRole.INSPECTOR);
        user2.setOrganization(organization);

        User user3 = new User();
        user3.setEmail("user1@org2.com");
        user3.setName("Org2 User1");
        user3.setPassword("password");
        user3.setRole(UserRole.SUPERVISOR);
        user3.setOrganization(org2);

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
        entityManager.clear();

        List<User> org1Users = entityManager
            .createQuery("SELECT u FROM User u WHERE u.organization.id = :orgId", User.class)
            .setParameter("orgId", organization.getId())
            .getResultList();

        assertThat(org1Users).hasSize(2);
        assertThat(org1Users).extracting(User::getEmail)
            .containsExactlyInAnyOrder("user1@org1.com", "user2@org1.com");
    }

    @Test
    void testFindUsersByRole() {
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setName("Admin");
        admin.setPassword("password");
        admin.setRole(UserRole.ADMIN);
        admin.setOrganization(organization);

        User inspector1 = new User();
        inspector1.setEmail("inspector1@test.com");
        inspector1.setName("Inspector 1");
        inspector1.setPassword("password");
        inspector1.setRole(UserRole.INSPECTOR);
        inspector1.setOrganization(organization);

        User inspector2 = new User();
        inspector2.setEmail("inspector2@test.com");
        inspector2.setName("Inspector 2");
        inspector2.setPassword("password");
        inspector2.setRole(UserRole.INSPECTOR);
        inspector2.setOrganization(organization);

        entityManager.persist(admin);
        entityManager.persist(inspector1);
        entityManager.persist(inspector2);
        entityManager.flush();
        entityManager.clear();

        List<User> inspectors = entityManager
            .createQuery("SELECT u FROM User u WHERE u.role = :role", User.class)
            .setParameter("role", UserRole.INSPECTOR)
            .getResultList();

        assertThat(inspectors).hasSize(2);
        assertThat(inspectors).extracting(User::getRole)
            .containsOnly(UserRole.INSPECTOR);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("update@example.com");
        user.setName("Original Name");
        user.setPassword("originalPassword");
        user.setRole(UserRole.INSPECTOR);
        user.setOrganization(organization);

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User toUpdate = entityManager.find(User.class, user.getId());
        toUpdate.setName("Updated Name");
        toUpdate.setPassword("updatedPassword");
        toUpdate.setRole(UserRole.SUPERVISOR);
        
        entityManager.flush();
        entityManager.clear();

        User updated = entityManager.find(User.class, user.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getPassword()).isEqualTo("updatedPassword");
        assertThat(updated.getRole()).isEqualTo(UserRole.SUPERVISOR);
        assertThat(updated.getEmail()).isEqualTo("update@example.com");
    }
}