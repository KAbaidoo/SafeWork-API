package com.safework.api.domain.user.integration;

import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.department.repository.DepartmentRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.dto.ChangePasswordRequest;
import com.safework.api.domain.user.dto.CreateUserRequest;
import com.safework.api.domain.user.dto.UpdateUserRequest;
import com.safework.api.domain.user.dto.UserDto;
import com.safework.api.domain.user.dto.UserProfileDto;
import com.safework.api.domain.user.mapper.UserMapper;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.domain.user.service.UserService;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    private Organization organization1;
    private Organization organization2;
    private Department department1;
    private Department department2;
    private Department org2Department;
    private User adminUser1;
    private User supervisorUser1;
    private User inspectorUser1;
    private User adminUser2;
    private User managerUser1;

    @BeforeEach
    void setUp() {
        // Setup password encoder
        passwordEncoder = new BCryptPasswordEncoder();

        // Create user service with real dependencies
        userService = new UserService(
                userRepository,
                departmentRepository,
                new UserMapper(),
                passwordEncoder
        );

        // Setup test organizations
        organization1 = new Organization();
        organization1.setName("Tech Solutions Inc");
        entityManager.persistAndFlush(organization1);

        organization2 = new Organization();
        organization2.setName("Manufacturing Corp");
        entityManager.persistAndFlush(organization2);

        // Setup test departments
        department1 = new Department();
        department1.setName("IT Department");
        department1.setCode("IT");
        department1.setOrganization(organization1);
        entityManager.persistAndFlush(department1);

        department2 = new Department();
        department2.setName("HR Department");
        department2.setCode("HR");
        department2.setOrganization(organization1);
        entityManager.persistAndFlush(department2);

        org2Department = new Department();
        org2Department.setName("Manufacturing");
        org2Department.setCode("MFG");
        org2Department.setOrganization(organization2);
        entityManager.persistAndFlush(org2Department);

        // Setup test users
        adminUser1 = new User();
        adminUser1.setName("Admin User 1");
        adminUser1.setEmail("admin1@techsolutions.com");
        adminUser1.setPassword(passwordEncoder.encode("password123"));
        adminUser1.setRole(UserRole.ADMIN);
        adminUser1.setOrganization(organization1);
        adminUser1.setDepartment(department1);
        entityManager.persistAndFlush(adminUser1);

        supervisorUser1 = new User();
        supervisorUser1.setName("Supervisor User 1");
        supervisorUser1.setEmail("supervisor1@techsolutions.com");
        supervisorUser1.setPassword(passwordEncoder.encode("password123"));
        supervisorUser1.setRole(UserRole.SUPERVISOR);
        supervisorUser1.setOrganization(organization1);
        supervisorUser1.setDepartment(department1);
        entityManager.persistAndFlush(supervisorUser1);

        inspectorUser1 = new User();
        inspectorUser1.setName("Inspector User 1");
        inspectorUser1.setEmail("inspector1@techsolutions.com");
        inspectorUser1.setPassword(passwordEncoder.encode("password123"));
        inspectorUser1.setRole(UserRole.INSPECTOR);
        inspectorUser1.setOrganization(organization1);
        inspectorUser1.setDepartment(department2);
        entityManager.persistAndFlush(inspectorUser1);

        adminUser2 = new User();
        adminUser2.setName("Admin User 2");
        adminUser2.setEmail("admin2@mfgcorp.com");
        adminUser2.setPassword(passwordEncoder.encode("password123"));
        adminUser2.setRole(UserRole.ADMIN);
        adminUser2.setOrganization(organization2);
        adminUser2.setDepartment(org2Department);
        entityManager.persistAndFlush(adminUser2);

        managerUser1 = new User();
        managerUser1.setName("Manager User 1");
        managerUser1.setEmail("manager1@techsolutions.com");
        managerUser1.setPassword(passwordEncoder.encode("password123"));
        managerUser1.setRole(UserRole.SUPERVISOR);
        managerUser1.setOrganization(organization1);
        // No department assigned initially
        entityManager.persistAndFlush(managerUser1);

        entityManager.clear();
    }

    @Test
    void createUser_ShouldCreateWithAllFields_AndPersistToDatabase() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "John Doe",
                "john.doe@techsolutions.com",
                "newpassword123",
                "INSPECTOR",
                department1.getId()
        );

        // When
        UserDto result = userService.createUser(request, adminUser1);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john.doe@techsolutions.com");
        assertThat(result.role()).isEqualTo("INSPECTOR");
        assertThat(result.organizationId()).isEqualTo(organization1.getId());
        assertThat(result.departmentId()).isEqualTo(department1.getId());
        assertThat(result.createdAt()).isNotNull();

        // Verify persistence
        User saved = userRepository.findById(result.id()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getEmail()).isEqualTo("john.doe@techsolutions.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.INSPECTOR);
        assertThat(saved.getOrganization().getId()).isEqualTo(organization1.getId());
        assertThat(saved.getDepartment().getId()).isEqualTo(department1.getId());
        assertThat(passwordEncoder.matches("newpassword123", saved.getPassword())).isTrue();
    }

    @Test
    void createUser_ShouldCreateWithMinimalFields_WithoutDepartment() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "Jane Smith",
                "jane.smith@techsolutions.com",
                "password456",
                "ADMIN",
                null  // no department
        );

        // When
        UserDto result = userService.createUser(request, adminUser1);

        // Then
        assertThat(result.name()).isEqualTo("Jane Smith");
        assertThat(result.email()).isEqualTo("jane.smith@techsolutions.com");
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.departmentId()).isNull();
        assertThat(result.organizationId()).isEqualTo(organization1.getId());

        // Verify persistence
        User saved = userRepository.findById(result.id()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getDepartment()).isNull();
        assertThat(saved.getOrganization().getId()).isEqualTo(organization1.getId());
    }

    @Test
    void createUser_ShouldThrowConflictException_WhenEmailAlreadyExists() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "Duplicate User",
                "admin1@techsolutions.com", // Already exists
                "password123",
                "INSPECTOR",
                department1.getId()
        );

        // When/Then
        assertThatThrownBy(() -> userService.createUser(request, adminUser1))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User with email admin1@techsolutions.com already exists");
    }

    @Test
    void createUser_ShouldThrowIllegalArgumentException_WhenInvalidRole() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "Invalid Role User",
                "invalid@techsolutions.com",
                "password123",
                "INVALID_ROLE",
                department1.getId()
        );

        // When/Then
        assertThatThrownBy(() -> userService.createUser(request, adminUser1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid role: INVALID_ROLE");
    }

    @Test
    void createUser_ShouldThrowResourceNotFoundException_WhenDepartmentNotFound() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "Department Not Found User",
                "notfound@techsolutions.com",
                "password123",
                "INSPECTOR",
                999L  // Non-existent department
        );

        // When/Then
        assertThatThrownBy(() -> userService.createUser(request, adminUser1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Department not found with id: 999");
    }

    @Test
    void createUser_ShouldThrowAccessDeniedException_WhenDepartmentFromDifferentOrganization() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "Cross Org User",
                "crossorg@techsolutions.com",
                "password123",
                "INSPECTOR",
                org2Department.getId()  // Department from different organization
        );

        // When/Then
        assertThatThrownBy(() -> userService.createUser(request, adminUser1))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Department does not belong to your organization");
    }

    @Test
    void findAllByOrganization_ShouldReturnOrganizationUsers_WithPagination() {
        // Given - Users already exist in setup (4 users in org1, 1 user in org2)
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDto> result = userService.findAllByOrganization(organization1.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(4);  // adminUser1, supervisorUser1, inspectorUser1, managerUser1
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).extracting(UserDto::email)
                .containsExactlyInAnyOrder(
                        "admin1@techsolutions.com",
                        "supervisor1@techsolutions.com", 
                        "inspector1@techsolutions.com",
                        "manager1@techsolutions.com"
                );

        // Verify organization isolation
        Page<UserDto> org2Result = userService.findAllByOrganization(organization2.getId(), pageable);
        assertThat(org2Result.getContent()).hasSize(1);
        assertThat(org2Result.getContent().get(0).email()).isEqualTo("admin2@mfgcorp.com");
    }

    @Test
    void findUserById_ShouldReturnCompleteUserDetails() {
        // Given
        Long userId = adminUser1.getId();

        // When
        UserDto result = userService.findUserById(userId, adminUser1);

        // Then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo("Admin User 1");
        assertThat(result.email()).isEqualTo("admin1@techsolutions.com");
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.organizationId()).isEqualTo(organization1.getId());
        assertThat(result.departmentId()).isEqualTo(department1.getId());
        assertThat(result.createdAt()).isNotNull();
    }

    @Test
    void findUserById_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> userService.findUserById(999L, adminUser1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");
    }

    @Test
    void findUserById_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // When/Then - User from organization2 trying to access user from organization1
        assertThatThrownBy(() -> userService.findUserById(adminUser1.getId(), adminUser2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this user");
    }

    @Test
    void getCurrentUserProfile_ShouldReturnCompleteProfile() {
        // When
        UserProfileDto result = userService.getCurrentUserProfile(adminUser1);

        // Then
        assertThat(result.id()).isEqualTo(adminUser1.getId());
        assertThat(result.name()).isEqualTo("Admin User 1");
        assertThat(result.email()).isEqualTo("admin1@techsolutions.com");
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.organizationName()).isEqualTo("Tech Solutions Inc");
        assertThat(result.departmentName()).isEqualTo("IT Department");
        assertThat(result.createdAt()).isNotNull();
    }

    @Test
    void getCurrentUserProfile_ShouldReturnProfileWithNullDepartment_WhenNoDepartmentAssigned() {
        // When
        UserProfileDto result = userService.getCurrentUserProfile(managerUser1);

        // Then
        assertThat(result.name()).isEqualTo("Manager User 1");
        assertThat(result.departmentName()).isNull();
        assertThat(result.organizationName()).isEqualTo("Tech Solutions Inc");
    }

    @Test
    void updateUser_ShouldUpdateAllFields_AndPersistChanges() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Admin User",
                "updated.admin@techsolutions.com",
                "SUPERVISOR",
                department2.getId()
        );

        // When
        UserDto result = userService.updateUser(adminUser1.getId(), updateRequest, adminUser1);

        // Then
        assertThat(result.name()).isEqualTo("Updated Admin User");
        assertThat(result.email()).isEqualTo("updated.admin@techsolutions.com");
        assertThat(result.role()).isEqualTo("SUPERVISOR");
        assertThat(result.departmentId()).isEqualTo(department2.getId());

        // Verify persistence
        entityManager.flush();
        entityManager.clear();
        User updated = userRepository.findById(adminUser1.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Admin User");
        assertThat(updated.getEmail()).isEqualTo("updated.admin@techsolutions.com");
        assertThat(updated.getRole()).isEqualTo(UserRole.SUPERVISOR);
        assertThat(updated.getDepartment().getId()).isEqualTo(department2.getId());
    }

    @Test
    void updateUser_ShouldSetDepartmentToNull_WhenDepartmentIdIsNull() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "No Department User",
                "nodept@techsolutions.com",
                "INSPECTOR",
                null  // Remove department assignment
        );

        // When
        UserDto result = userService.updateUser(adminUser1.getId(), updateRequest, adminUser1);

        // Then
        assertThat(result.departmentId()).isNull();

        // Verify persistence
        entityManager.flush();
        entityManager.clear();
        User updated = userRepository.findById(adminUser1.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getDepartment()).isNull();
    }

    @Test
    void updateUser_ShouldThrowConflictException_WhenNewEmailAlreadyExists() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Name",
                "supervisor1@techsolutions.com", // Email already used by supervisorUser1
                "ADMIN",
                department1.getId()
        );

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(adminUser1.getId(), updateRequest, adminUser1))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User with email supervisor1@techsolutions.com already exists");
    }

    @Test
    void updateUser_ShouldAllowSameEmail_WhenUpdatingSameUser() {
        // Given - Keep the same email but change other fields
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Admin",
                "admin1@techsolutions.com", // Same email as current user
                "SUPERVISOR",
                department2.getId()
        );

        // When
        UserDto result = userService.updateUser(adminUser1.getId(), updateRequest, adminUser1);

        // Then
        assertThat(result.name()).isEqualTo("Updated Admin");
        assertThat(result.email()).isEqualTo("admin1@techsolutions.com");
        assertThat(result.role()).isEqualTo("SUPERVISOR");
    }

    @Test
    void deleteUser_ShouldDeleteSuccessfully() {
        // Given
        Long userToDeleteId = inspectorUser1.getId();

        // When
        userService.deleteUser(userToDeleteId, adminUser1);

        // Then
        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.findById(userToDeleteId)).isEmpty();
    }

    @Test
    void deleteUser_ShouldThrowConflictException_WhenTryingToDeleteSelf() {
        // When/Then
        assertThatThrownBy(() -> userService.deleteUser(adminUser1.getId(), adminUser1))
                .isInstanceOf(ConflictException.class)
                .hasMessage("You cannot delete your own account");
    }

    @Test
    void deleteUser_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // When/Then - User from organization2 trying to delete user from organization1
        assertThatThrownBy(() -> userService.deleteUser(adminUser1.getId(), adminUser2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this user");
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenCurrentPasswordIsCorrect() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(
                "password123", // Current password
                "newpassword456"
        );

        // When
        userService.changePassword(request, adminUser1);

        // Then
        entityManager.flush();
        entityManager.clear();
        User updated = userRepository.findById(adminUser1.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(passwordEncoder.matches("newpassword456", updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("password123", updated.getPassword())).isFalse();
    }

    @Test
    void changePassword_ShouldThrowIllegalArgumentException_WhenCurrentPasswordIsIncorrect() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongpassword", // Incorrect current password
                "newpassword456"
        );

        // When/Then
        assertThatThrownBy(() -> userService.changePassword(request, adminUser1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect");
    }

    @Test
    void multiTenantSecurity_ShouldEnforceOrganizationBoundaries() {
        // Given - Create users in both organizations (already done in setup)
        CreateUserRequest org1Request = new CreateUserRequest(
                "Org1 User",
                "org1user@techsolutions.com",
                "password123",
                "INSPECTOR",
                department1.getId()
        );
        CreateUserRequest org2Request = new CreateUserRequest(
                "Org2 User", 
                "org2user@mfgcorp.com",
                "password123",
                "INSPECTOR",
                org2Department.getId()
        );

        UserDto org1User = userService.createUser(org1Request, adminUser1);
        UserDto org2User = userService.createUser(org2Request, adminUser2);

        // When/Then - Users should only access users in their organization
        UserDto org1Result = userService.findUserById(org1User.id(), adminUser1);
        assertThat(org1Result).isNotNull();

        // Cross-organization access should fail
        assertThatThrownBy(() -> userService.findUserById(org1User.id(), adminUser2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this user");

        assertThatThrownBy(() -> userService.findUserById(org2User.id(), adminUser1))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this user");

        // Update and delete should also respect organization boundaries
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Cross-org attack",
                "malicious@attack.com",
                "ADMIN",
                org2Department.getId()
        );

        assertThatThrownBy(() -> userService.updateUser(org1User.id(), updateRequest, adminUser2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this user");

        assertThatThrownBy(() -> userService.deleteUser(org1User.id(), adminUser2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this user");
    }

    @Test
    void userLifecycle_ShouldMaintainRelationshipsWithDepartmentAndOrganization() {
        // Given - Create complete user setup
        CreateUserRequest createRequest = new CreateUserRequest(
                "Complete User",
                "complete@techsolutions.com",
                "password123",
                "SUPERVISOR",
                department1.getId()
        );

        // When - Create user
        UserDto created = userService.createUser(createRequest, adminUser1);

        // Update user's department
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Complete User",
                "updated.complete@techsolutions.com",
                "INSPECTOR",
                department2.getId()
        );
        UserDto updated = userService.updateUser(created.id(), updateRequest, adminUser1);

        entityManager.flush();
        entityManager.clear();

        // Then - Verify all relationships
        User reloaded = userRepository.findById(created.id()).orElse(null);
        assertThat(reloaded).isNotNull();

        // Check organization relationship
        assertThat(reloaded.getOrganization()).isNotNull();
        assertThat(reloaded.getOrganization().getId()).isEqualTo(organization1.getId());
        assertThat(reloaded.getOrganization().getName()).isEqualTo("Tech Solutions Inc");

        // Check department relationship
        assertThat(reloaded.getDepartment()).isNotNull();
        assertThat(reloaded.getDepartment().getId()).isEqualTo(department2.getId());
        assertThat(reloaded.getDepartment().getName()).isEqualTo("HR Department");

        // Check updated fields
        assertThat(reloaded.getName()).isEqualTo("Updated Complete User");
        assertThat(reloaded.getEmail()).isEqualTo("updated.complete@techsolutions.com");
        assertThat(reloaded.getRole()).isEqualTo(UserRole.INSPECTOR);
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
    }

    @Test
    void passwordSecurity_ShouldHashAndValidatePasswords() {
        // Given
        String rawPassword = "securepassword123";
        CreateUserRequest createRequest = new CreateUserRequest(
                "Security Test User",
                "security@techsolutions.com",
                rawPassword,
                "INSPECTOR",
                department1.getId()
        );

        // When - Create user
        UserDto created = userService.createUser(createRequest, adminUser1);

        // Then - Verify password is hashed
        entityManager.flush();
        entityManager.clear();
        User saved = userRepository.findById(created.id()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo(rawPassword); // Should be hashed
        assertThat(passwordEncoder.matches(rawPassword, saved.getPassword())).isTrue(); // But should match

        // Test password change
        ChangePasswordRequest changeRequest = new ChangePasswordRequest(
                rawPassword,
                "newSecurePassword456"
        );

        userService.changePassword(changeRequest, saved);

        entityManager.flush();
        entityManager.clear();
        User afterChange = userRepository.findById(created.id()).orElse(null);
        assertThat(afterChange).isNotNull();
        assertThat(passwordEncoder.matches("newSecurePassword456", afterChange.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, afterChange.getPassword())).isFalse();
    }
}