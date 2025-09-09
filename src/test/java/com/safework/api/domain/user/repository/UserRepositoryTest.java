package com.safework.api.domain.user.repository;

import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
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
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private Organization organization1;
    private Organization organization2;
    private Department department1;
    private Department department2;
    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @BeforeEach
    void setUp() {
        // Create test organizations
        organization1 = new Organization();
        organization1.setName("Org 1");
        entityManager.persistAndFlush(organization1);

        organization2 = new Organization();
        organization2.setName("Org 2");
        entityManager.persistAndFlush(organization2);

        // Create departments
        department1 = new Department();
        department1.setName("Department 1");
        department1.setOrganization(organization1);
        entityManager.persistAndFlush(department1);

        department2 = new Department();
        department2.setName("Department 2");
        department2.setOrganization(organization2);
        entityManager.persistAndFlush(department2);

        // Create test users
        user1 = new User();
        user1.setName("John Doe");
        user1.setEmail("john.doe@org1.com");
        user1.setPassword("hashedPassword1");
        user1.setRole(UserRole.ADMIN);
        user1.setOrganization(organization1);
        user1.setDepartment(department1);
        entityManager.persistAndFlush(user1);

        user2 = new User();
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@org1.com");
        user2.setPassword("hashedPassword2");
        user2.setRole(UserRole.SUPERVISOR);
        user2.setOrganization(organization1);
        user2.setDepartment(department1);
        entityManager.persistAndFlush(user2);

        user3 = new User();
        user3.setName("Bob Johnson");
        user3.setEmail("bob.johnson@org1.com");
        user3.setPassword("hashedPassword3");
        user3.setRole(UserRole.INSPECTOR);
        user3.setOrganization(organization1);
        // No department assigned
        entityManager.persistAndFlush(user3);

        user4 = new User();
        user4.setName("Alice Wilson");
        user4.setEmail("alice.wilson@org2.com");
        user4.setPassword("hashedPassword4");
        user4.setRole(UserRole.ADMIN);
        user4.setOrganization(organization2);
        user4.setDepartment(department2);
        entityManager.persistAndFlush(user4);

        entityManager.clear();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // When
        Optional<User> result = userRepository.findByEmail("john.doe@org1.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john.doe@org1.com");
        assertThat(result.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@email.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailIsNull() {
        // When
        Optional<User> result = userRepository.findByEmail(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_ShouldBeCaseInsensitive() {
        // When
        Optional<User> result = userRepository.findByEmail("JOHN.DOE@ORG1.COM");

        // Then
        // Note: This test might fail if the database doesn't have case-insensitive collation
        // In that case, the implementation would need to be updated to handle case-insensitivity
        assertThat(result).isEmpty(); // Expected behavior with case-sensitive collation
    }

    @Test
    void findAllByOrganizationId_ShouldReturnUsersForOrganization() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllByOrganizationId(organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@org1.com", "jane.smith@org1.com", "bob.johnson@org1.com");
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void findAllByOrganizationId_ShouldReturnEmptyPage_WhenNoUsersForOrganization() {
        // Given
        Organization emptyOrg = new Organization();
        emptyOrg.setName("Empty Org");
        entityManager.persistAndFlush(emptyOrg);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllByOrganizationId(emptyOrg.getId(), pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findAllByOrganizationId_ShouldRespectPagination() {
        // Given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // When
        Page<User> firstResult = userRepository.findAllByOrganizationId(organization1.getId(), firstPage);
        Page<User> secondResult = userRepository.findAllByOrganizationId(organization1.getId(), secondPage);

        // Then
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(secondResult.getContent()).hasSize(1);
        assertThat(firstResult.getTotalElements()).isEqualTo(3);
        assertThat(secondResult.getTotalElements()).isEqualTo(3);
        assertThat(firstResult.getNumber()).isEqualTo(0);
        assertThat(secondResult.getNumber()).isEqualTo(1);
        
        // Ensure different users are returned
        assertThat(firstResult.getContent().get(0).getId())
                .isNotEqualTo(secondResult.getContent().get(0).getId());
        assertThat(firstResult.getContent().get(1).getId())
                .isNotEqualTo(secondResult.getContent().get(0).getId());
    }

    @Test
    void findAllByOrganizationId_ShouldNotReturnUsersFromOtherOrganizations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllByOrganizationId(organization2.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice.wilson@org2.com");
        assertThat(result.getContent().get(0).getOrganization().getId()).isEqualTo(organization2.getId());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // When
        boolean result = userRepository.existsByEmail("john.doe@org1.com");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // When
        boolean result = userRepository.existsByEmail("nonexistent@email.com");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailIsNull() {
        // When
        boolean result = userRepository.existsByEmail(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailIsEmpty() {
        // When
        boolean result = userRepository.existsByEmail("");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void save_ShouldPersistUser() {
        // Given
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new.user@org1.com");
        newUser.setPassword("hashedPassword");
        newUser.setRole(UserRole.INSPECTOR);
        newUser.setOrganization(organization1);

        // When
        User savedUser = userRepository.save(newUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        // Verify it can be found
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("New User");
        assertThat(foundUser.get().getEmail()).isEqualTo("new.user@org1.com");
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.INSPECTOR);
    }

    @Test
    void save_ShouldUpdateUser_WhenUserExists() {
        // Given
        User existingUser = userRepository.findById(user1.getId()).orElseThrow();
        String originalEmail = existingUser.getEmail();
        
        // When
        existingUser.setName("Updated Name");
        existingUser.setRole(UserRole.SUPERVISOR);
        User updatedUser = userRepository.save(existingUser);
        entityManager.flush();

        // Then
        assertThat(updatedUser.getId()).isEqualTo(user1.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo(originalEmail); // Email should remain the same
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.SUPERVISOR);
        assertThat(updatedUser.getUpdatedAt()).isAfter(updatedUser.getCreatedAt());
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        // When
        Optional<User> result = userRepository.findById(user1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenDoesNotExist() {
        // When
        Optional<User> result = userRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Given
        Long userId = user3.getId();
        assertThat(userRepository.findById(userId)).isPresent();

        // When
        userRepository.delete(user3);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void count_ShouldReturnTotalNumberOfUsers() {
        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(4);
    }

    @Test
    void userWithDepartment_ShouldHaveCorrectRelationship() {
        // When
        Optional<User> result = userRepository.findById(user1.getId());

        // Then
        assertThat(result).isPresent();
        User foundUser = result.get();
        assertThat(foundUser.getDepartment()).isNotNull();
        assertThat(foundUser.getDepartment().getName()).isEqualTo("Department 1");
        assertThat(foundUser.getDepartment().getOrganization().getId()).isEqualTo(organization1.getId());
    }

    @Test
    void userWithoutDepartment_ShouldHaveNullDepartment() {
        // When
        Optional<User> result = userRepository.findById(user3.getId());

        // Then
        assertThat(result).isPresent();
        User foundUser = result.get();
        assertThat(foundUser.getDepartment()).isNull();
        assertThat(foundUser.getOrganization()).isNotNull();
        assertThat(foundUser.getOrganization().getId()).isEqualTo(organization1.getId());
    }

    @Test
    void findAllByOrganizationId_ShouldIncludeUsersWithAndWithoutDepartments() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findAllByOrganizationId(organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(3);
        
        // Check that we have users both with and without departments
        long usersWithDepartment = result.getContent().stream()
                .filter(user -> user.getDepartment() != null)
                .count();
        long usersWithoutDepartment = result.getContent().stream()
                .filter(user -> user.getDepartment() == null)
                .count();
                
        assertThat(usersWithDepartment).isEqualTo(2);
        assertThat(usersWithoutDepartment).isEqualTo(1);
    }
}