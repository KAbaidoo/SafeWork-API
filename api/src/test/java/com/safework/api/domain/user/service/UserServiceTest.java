package com.safework.api.domain.user.service;

import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.department.repository.DepartmentRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.dto.*;
import com.safework.api.domain.user.mapper.UserMapper;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User currentUser;
    private Organization organization;
    private Organization otherOrganization;
    private Department department;
    private Department otherOrgDepartment;
    private User targetUser;
    private UserDto userDto;
    private UserProfileDto userProfileDto;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization = createMockOrganization(1L, "Test Organization");

        otherOrganization = new Organization();
        otherOrganization = createMockOrganization(2L, "Other Organization");

        department = new Department();
        setEntityId(department, 1L);
        department.setName("Test Department");
        department.setOrganization(organization);

        otherOrgDepartment = new Department();
        setEntityId(otherOrgDepartment, 2L);
        otherOrgDepartment.setName("Other Department");
        otherOrgDepartment.setOrganization(otherOrganization);

        currentUser = new User();
        setEntityId(currentUser, 1L);
        currentUser.setName("Current User");
        currentUser.setEmail("current@test.com");
        currentUser.setPassword("hashedPassword");
        currentUser.setRole(UserRole.ADMIN);
        currentUser.setOrganization(organization);

        targetUser = new User();
        setEntityId(targetUser, 2L);
        targetUser.setName("Target User");
        targetUser.setEmail("target@test.com");
        targetUser.setPassword("hashedPassword");
        targetUser.setRole(UserRole.SUPERVISOR);
        targetUser.setOrganization(organization);
        targetUser.setDepartment(department);

        userDto = new UserDto(2L, "Target User", "target@test.com", "SUPERVISOR", 1L, 1L, LocalDateTime.now());
        userProfileDto = new UserProfileDto(1L, "Current User", "current@test.com", "ADMIN", "Test Organization", null, LocalDateTime.now());

        createRequest = new CreateUserRequest(
            "New User",
            "new@test.com",
            "password123",
            "INSPECTOR",
            1L
        );

        updateRequest = new UpdateUserRequest(
            "Updated User",
            "updated@test.com",
            "SUPERVISOR",
            1L
        );

        changePasswordRequest = new ChangePasswordRequest(
            "currentPassword",
            "newPassword123"
        );
    }

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
    void createUser_ShouldCreateUser_WhenValidRequest() {
        // Given
        User savedUser = new User();
        savedUser.setName("New User");
        savedUser.setEmail("new@test.com");
        savedUser.setOrganization(organization);
        savedUser.setDepartment(department);
        savedUser.setRole(UserRole.INSPECTOR);

        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(passwordEncoder.encode("password123")).willReturn("hashedPassword123");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(userMapper.toDto(savedUser)).willReturn(userDto);

        // When
        UserDto result = userService.createUser(createRequest, currentUser);

        // Then
        assertThat(result).isEqualTo(userDto);

        then(userRepository).should().existsByEmail("new@test.com");
        then(departmentRepository).should().findById(1L);
        then(passwordEncoder).should().encode("password123");
        then(userRepository).should().save(any(User.class));
        then(userMapper).should().toDto(savedUser);
    }

    @Test
    void createUser_ShouldThrowConflictException_WhenEmailAlreadyExists() {
        // Given
        given(userRepository.existsByEmail("new@test.com")).willReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.createUser(createRequest, currentUser))
            .isInstanceOf(ConflictException.class)
            .hasMessage("User with email new@test.com already exists");

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowIllegalArgumentException_WhenInvalidRole() {
        // Given
        CreateUserRequest invalidRoleRequest = new CreateUserRequest(
            "New User",
            "new@test.com",
            "password123",
            "INVALID_ROLE",
            1L
        );
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.createUser(invalidRoleRequest, currentUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid role: INVALID_ROLE");

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowResourceNotFoundException_WhenDepartmentNotFound() {
        // Given
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(departmentRepository.findById(1L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.createUser(createRequest, currentUser))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Department not found with id: 1");

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowAccessDeniedException_WhenDepartmentFromDifferentOrganization() {
        // Given
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(departmentRepository.findById(1L)).willReturn(Optional.of(otherOrgDepartment));

        // When/Then
        assertThatThrownBy(() -> userService.createUser(createRequest, currentUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Department does not belong to your organization");

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldCreateUserWithoutDepartment_WhenDepartmentIdIsNull() {
        // Given
        CreateUserRequest requestWithoutDepartment = new CreateUserRequest(
            "New User",
            "new@test.com",
            "password123",
            "INSPECTOR",
            null
        );
        User savedUser = new User();
        savedUser.setName("New User");
        savedUser.setEmail("new@test.com");
        savedUser.setOrganization(organization);
        savedUser.setRole(UserRole.INSPECTOR);

        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("hashedPassword123");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(userMapper.toDto(savedUser)).willReturn(userDto);

        // When
        UserDto result = userService.createUser(requestWithoutDepartment, currentUser);

        // Then
        assertThat(result).isEqualTo(userDto);

        then(departmentRepository).should(never()).findById(any());
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void findAllByOrganization_ShouldReturnPagedUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(targetUser);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);

        given(userRepository.findAllByOrganizationId(1L, pageable)).willReturn(userPage);
        given(userMapper.toDto(targetUser)).willReturn(userDto);

        // When
        Page<UserDto> result = userService.findAllByOrganization(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(userDto);
        assertThat(result.getTotalElements()).isEqualTo(1);

        then(userRepository).should().findAllByOrganizationId(1L, pageable);
        then(userMapper).should().toDto(targetUser);
    }

    @Test
    void findUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(userMapper.toDto(targetUser)).willReturn(userDto);

        // When
        UserDto result = userService.findUserById(2L, currentUser);

        // Then
        assertThat(result).isEqualTo(userDto);

        then(userRepository).should().findById(2L);
        then(userMapper).should().toDto(targetUser);
    }

    @Test
    void findUserById_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Given
        given(userRepository.findById(2L)).willReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.findUserById(2L, currentUser))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User not found with id: 2");

        then(userMapper).should(never()).toDto(any(User.class));
    }

    @Test
    void findUserById_ShouldThrowAccessDeniedException_WhenUserFromDifferentOrganization() {
        // Given
        targetUser.setOrganization(otherOrganization);
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));

        // When/Then
        assertThatThrownBy(() -> userService.findUserById(2L, currentUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You do not have permission to access this user");

        then(userMapper).should(never()).toDto(any(User.class));
    }

    @Test
    void getCurrentUserProfile_ShouldReturnUserProfile() {
        // Given
        given(userMapper.toProfileDto(currentUser)).willReturn(userProfileDto);

        // When
        UserProfileDto result = userService.getCurrentUserProfile(currentUser);

        // Then
        assertThat(result).isEqualTo(userProfileDto);

        then(userMapper).should().toProfileDto(currentUser);
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenValidRequest() {
        // Given
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(userRepository.existsByEmail("updated@test.com")).willReturn(false);
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(userRepository.save(targetUser)).willReturn(targetUser);
        given(userMapper.toDto(targetUser)).willReturn(userDto);

        // When
        UserDto result = userService.updateUser(2L, updateRequest, currentUser);

        // Then
        assertThat(result).isEqualTo(userDto);
        assertThat(targetUser.getName()).isEqualTo("Updated User");
        assertThat(targetUser.getEmail()).isEqualTo("updated@test.com");
        assertThat(targetUser.getRole()).isEqualTo(UserRole.SUPERVISOR);

        then(userRepository).should().findById(2L);
        then(userRepository).should().save(targetUser);
        then(userMapper).should().toDto(targetUser);
    }

    @Test
    void updateUser_ShouldThrowConflictException_WhenEmailAlreadyExists() {
        // Given
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(userRepository.existsByEmail("updated@test.com")).willReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(2L, updateRequest, currentUser))
            .isInstanceOf(ConflictException.class)
            .hasMessage("User with email updated@test.com already exists");

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldNotCheckEmailConflict_WhenEmailUnchanged() {
        // Given
        UpdateUserRequest sameEmailRequest = new UpdateUserRequest(
            "Updated User",
            "target@test.com", // Same email as targetUser
            "SUPERVISOR",
            1L
        );
        
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
        given(userRepository.save(targetUser)).willReturn(targetUser);
        given(userMapper.toDto(targetUser)).willReturn(userDto);

        // When
        UserDto result = userService.updateUser(2L, sameEmailRequest, currentUser);

        // Then
        assertThat(result).isEqualTo(userDto);

        then(userRepository).should(never()).existsByEmail(anyString());
        then(userRepository).should().save(targetUser);
    }

    @Test
    void updateUser_ShouldClearDepartment_WhenDepartmentIdIsNull() {
        // Given
        UpdateUserRequest requestWithNullDepartment = new UpdateUserRequest(
            "Updated User",
            "updated@test.com",
            "SUPERVISOR",
            null
        );
        
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));
        given(userRepository.existsByEmail("updated@test.com")).willReturn(false);
        given(userRepository.save(targetUser)).willReturn(targetUser);
        given(userMapper.toDto(targetUser)).willReturn(userDto);

        // When
        UserDto result = userService.updateUser(2L, requestWithNullDepartment, currentUser);

        // Then
        assertThat(result).isEqualTo(userDto);
        assertThat(targetUser.getDepartment()).isNull();

        then(departmentRepository).should(never()).findById(any());
        then(userRepository).should().save(targetUser);
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Given
        given(userRepository.findById(2L)).willReturn(Optional.of(targetUser));

        // When
        userService.deleteUser(2L, currentUser);

        // Then
        then(userRepository).should().findById(2L);
        then(userRepository).should().delete(targetUser);
    }

    @Test
    void deleteUser_ShouldThrowConflictException_WhenTryingToDeleteSelf() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(currentUser));

        // When/Then
        assertThatThrownBy(() -> userService.deleteUser(1L, currentUser))
            .isInstanceOf(ConflictException.class)
            .hasMessage("You cannot delete your own account");

        then(userRepository).should(never()).delete(any(User.class));
    }

    @Test
    void changePassword_ShouldChangePassword_WhenCurrentPasswordCorrect() {
        // Given
        given(passwordEncoder.matches("currentPassword", currentUser.getPassword())).willReturn(true);
        given(passwordEncoder.encode("newPassword123")).willReturn("newHashedPassword");
        given(userRepository.save(currentUser)).willReturn(currentUser);

        // When
        userService.changePassword(changePasswordRequest, currentUser);

        // Then
        assertThat(currentUser.getPassword()).isEqualTo("newHashedPassword");

        then(passwordEncoder).should().matches("currentPassword", "hashedPassword");
        then(passwordEncoder).should().encode("newPassword123");
        then(userRepository).should().save(currentUser);
    }

    @Test
    void changePassword_ShouldThrowIllegalArgumentException_WhenCurrentPasswordIncorrect() {
        // Given
        given(passwordEncoder.matches("currentPassword", currentUser.getPassword())).willReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.changePassword(changePasswordRequest, currentUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Current password is incorrect");

        then(passwordEncoder).should(never()).encode(anyString());
        then(userRepository).should(never()).save(any(User.class));
    }
}