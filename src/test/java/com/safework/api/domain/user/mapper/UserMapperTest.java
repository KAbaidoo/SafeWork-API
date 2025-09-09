package com.safework.api.domain.user.mapper;

import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.dto.UserDto;
import com.safework.api.domain.user.dto.UserProfileDto;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;
    private Organization organization;
    private Department department;
    private User userWithDepartment;
    private User userWithoutDepartment;
    private LocalDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        testCreatedAt = LocalDateTime.of(2023, 12, 1, 10, 30, 0);

        // Create organization
        organization = new Organization();
        setEntityId(organization, 1L);
        organization.setName("Test Organization");

        // Create department
        department = new Department();
        setEntityId(department, 1L);
        department.setName("Test Department");
        department.setOrganization(organization);

        // Create user with department
        userWithDepartment = new User();
        setEntityId(userWithDepartment, 1L);
        userWithDepartment.setName("John Doe");
        userWithDepartment.setEmail("john.doe@test.com");
        userWithDepartment.setRole(UserRole.ADMIN);
        userWithDepartment.setOrganization(organization);
        userWithDepartment.setDepartment(department);
        setCreatedAt(userWithDepartment, testCreatedAt);

        // Create user without department
        userWithoutDepartment = new User();
        setEntityId(userWithoutDepartment, 2L);
        userWithoutDepartment.setName("Jane Smith");
        userWithoutDepartment.setEmail("jane.smith@test.com");
        userWithoutDepartment.setRole(UserRole.SUPERVISOR);
        userWithoutDepartment.setOrganization(organization);
        // No department set
        setCreatedAt(userWithoutDepartment, testCreatedAt);
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

    private void setCreatedAt(User user, LocalDateTime createdAt) {
        try {
            Field createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt", e);
        }
    }

    @Test
    void toDto_ShouldMapUserToDtoCorrectly_WhenUserHasDepartment() {
        // When
        UserDto result = userMapper.toDto(userWithDepartment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john.doe@test.com");
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.departmentId()).isEqualTo(1L);
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
    }

    @Test
    void toDto_ShouldMapUserToDtoCorrectly_WhenUserHasNoDepartment() {
        // When
        UserDto result = userMapper.toDto(userWithoutDepartment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Jane Smith");
        assertThat(result.email()).isEqualTo("jane.smith@test.com");
        assertThat(result.role()).isEqualTo("SUPERVISOR");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.departmentId()).isNull();
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
    }

    @Test
    void toDto_ShouldHandleAllUserRoles() {
        // Test ADMIN role
        User adminUser = createUserWithRole(UserRole.ADMIN);
        UserDto adminDto = userMapper.toDto(adminUser);
        assertThat(adminDto.role()).isEqualTo("ADMIN");

        // Test SUPERVISOR role
        User supervisorUser = createUserWithRole(UserRole.SUPERVISOR);
        UserDto supervisorDto = userMapper.toDto(supervisorUser);
        assertThat(supervisorDto.role()).isEqualTo("SUPERVISOR");

        // Test INSPECTOR role
        User inspectorUser = createUserWithRole(UserRole.INSPECTOR);
        UserDto inspectorDto = userMapper.toDto(inspectorUser);
        assertThat(inspectorDto.role()).isEqualTo("INSPECTOR");
    }

    @Test
    void toProfileDto_ShouldMapUserToProfileDtoCorrectly_WhenUserHasDepartment() {
        // When
        UserProfileDto result = userMapper.toProfileDto(userWithDepartment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john.doe@test.com");
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.organizationName()).isEqualTo("Test Organization");
        assertThat(result.departmentName()).isEqualTo("Test Department");
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
    }

    @Test
    void toProfileDto_ShouldMapUserToProfileDtoCorrectly_WhenUserHasNoDepartment() {
        // When
        UserProfileDto result = userMapper.toProfileDto(userWithoutDepartment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Jane Smith");
        assertThat(result.email()).isEqualTo("jane.smith@test.com");
        assertThat(result.role()).isEqualTo("SUPERVISOR");
        assertThat(result.organizationName()).isEqualTo("Test Organization");
        assertThat(result.departmentName()).isNull();
        assertThat(result.createdAt()).isEqualTo(testCreatedAt);
    }

    @Test
    void toProfileDto_ShouldHandleAllUserRoles() {
        // Test ADMIN role
        User adminUser = createUserWithRole(UserRole.ADMIN);
        UserProfileDto adminDto = userMapper.toProfileDto(adminUser);
        assertThat(adminDto.role()).isEqualTo("ADMIN");

        // Test SUPERVISOR role
        User supervisorUser = createUserWithRole(UserRole.SUPERVISOR);
        UserProfileDto supervisorDto = userMapper.toProfileDto(supervisorUser);
        assertThat(supervisorDto.role()).isEqualTo("SUPERVISOR");

        // Test INSPECTOR role
        User inspectorUser = createUserWithRole(UserRole.INSPECTOR);
        UserProfileDto inspectorDto = userMapper.toProfileDto(inspectorUser);
        assertThat(inspectorDto.role()).isEqualTo("INSPECTOR");
    }

    @Test
    void toDto_ShouldPreserveAllFieldsFromEntity() {
        // Given
        LocalDateTime specificTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
        User user = new User();
        setEntityId(user, 999L);
        user.setName("Specific User");
        user.setEmail("specific@example.com");
        user.setRole(UserRole.INSPECTOR);
        user.setOrganization(organization);
        user.setDepartment(department);
        setCreatedAt(user, specificTime);

        // When
        UserDto result = userMapper.toDto(user);

        // Then
        assertThat(result.id()).isEqualTo(999L);
        assertThat(result.name()).isEqualTo("Specific User");
        assertThat(result.email()).isEqualTo("specific@example.com");
        assertThat(result.role()).isEqualTo("INSPECTOR");
        assertThat(result.organizationId()).isEqualTo(1L);
        assertThat(result.departmentId()).isEqualTo(1L);
        assertThat(result.createdAt()).isEqualTo(specificTime);
    }

    @Test
    void toProfileDto_ShouldPreserveAllFieldsFromEntity() {
        // Given
        LocalDateTime specificTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
        User user = new User();
        setEntityId(user, 999L);
        user.setName("Specific User");
        user.setEmail("specific@example.com");
        user.setRole(UserRole.INSPECTOR);
        user.setOrganization(organization);
        user.setDepartment(department);
        setCreatedAt(user, specificTime);

        // When
        UserProfileDto result = userMapper.toProfileDto(user);

        // Then
        assertThat(result.id()).isEqualTo(999L);
        assertThat(result.name()).isEqualTo("Specific User");
        assertThat(result.email()).isEqualTo("specific@example.com");
        assertThat(result.role()).isEqualTo("INSPECTOR");
        assertThat(result.organizationName()).isEqualTo("Test Organization");
        assertThat(result.departmentName()).isEqualTo("Test Department");
        assertThat(result.createdAt()).isEqualTo(specificTime);
    }

    @Test
    void toDto_ShouldHandleNullDepartmentGracefully() {
        // Given - userWithoutDepartment already has null department
        
        // When
        UserDto result = userMapper.toDto(userWithoutDepartment);

        // Then
        assertThat(result.departmentId()).isNull();
        // All other fields should still be mapped correctly
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Jane Smith");
        assertThat(result.organizationId()).isEqualTo(1L);
    }

    @Test
    void toProfileDto_ShouldHandleNullDepartmentGracefully() {
        // Given - userWithoutDepartment already has null department
        
        // When
        UserProfileDto result = userMapper.toProfileDto(userWithoutDepartment);

        // Then
        assertThat(result.departmentName()).isNull();
        // All other fields should still be mapped correctly
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Jane Smith");
        assertThat(result.organizationName()).isEqualTo("Test Organization");
    }

    private User createUserWithRole(UserRole role) {
        User user = new User();
        setEntityId(user, 100L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(role);
        user.setOrganization(organization);
        setCreatedAt(user, testCreatedAt);
        return user;
    }
}