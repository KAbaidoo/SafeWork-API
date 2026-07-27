package com.safework.api.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.dto.*;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.service.UserService;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import com.safework.api.security.PrincipalUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // Remove pre-created PrincipalUser objects
    private User adminUser;
    private User supervisorUser;
    private User inspectorUser;
    private Organization organization;
    private UserDto userDto;
    private UserProfileDto userProfileDto;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization = createMockOrganization(1L, "Test Organization");

        // Admin user
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setOrganization(organization);
        setEntityId(adminUser, 1L);

        // Supervisor user  
        supervisorUser = new User();
        supervisorUser.setName("Supervisor User");
        supervisorUser.setEmail("supervisor@test.com");
        supervisorUser.setRole(UserRole.SUPERVISOR);
        supervisorUser.setOrganization(organization);
        setEntityId(supervisorUser, 2L);

        // Inspector user
        inspectorUser = new User();
        inspectorUser.setName("Inspector User");
        inspectorUser.setEmail("inspector@test.com");
        inspectorUser.setRole(UserRole.INSPECTOR);
        inspectorUser.setOrganization(organization);
        setEntityId(inspectorUser, 3L);

        // PrincipalUser objects will be created inline in test methods

        userDto = new UserDto(2L, "Test User", "test@test.com", "SUPERVISOR", 1L, 1L, LocalDateTime.now());
        userProfileDto = new UserProfileDto(1L, "Admin User", "admin@test.com", "ADMIN", "Test Organization", null, LocalDateTime.now());

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

    // CREATE USER TESTS

    @Test
    void createUser_ShouldCreateUser_WhenAdminMakesValidRequest() throws Exception {
        // Given
        given(userService.createUser(any(CreateUserRequest.class), eq(adminUser)))
            .willReturn(userDto);

        // When & Then
        mockMvc.perform(post("/v1/users")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));
    }

    @Test
    void createUser_ShouldReturnForbidden_WhenNonAdminTriesToCreate() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/users")
                .with(user(new PrincipalUser(supervisorUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Given
        given(userService.createUser(any(CreateUserRequest.class), eq(adminUser)))
            .willThrow(new ConflictException("User with email new@test.com already exists"));

        // When & Then
        mockMvc.perform(post("/v1/users")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email new@test.com already exists"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        CreateUserRequest invalidRequest = new CreateUserRequest(
            "", // Empty name
            "invalid-email", // Invalid email format
            "123", // Too short password
            "INVALID_ROLE",
            null
        );

        // When & Then
        mockMvc.perform(post("/v1/users")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // GET USERS TESTS

    @Test
    void getUsersByOrganization_ShouldReturnUsers_WhenAdminRequests() throws Exception {
        // Given
        Page<UserDto> userPage = new PageImpl<>(List.of(userDto), PageRequest.of(0, 10), 1);
        given(userService.findAllByOrganization(eq(1L), any()))
            .willReturn(userPage);

        // When & Then
        mockMvc.perform(get("/v1/users")
                .with(user(new PrincipalUser(adminUser)))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getUsersByOrganization_ShouldReturnUsers_WhenSupervisorRequests() throws Exception {
        // Given
        Page<UserDto> userPage = new PageImpl<>(List.of(userDto), PageRequest.of(0, 10), 1);
        given(userService.findAllByOrganization(eq(1L), any()))
            .willReturn(userPage);

        // When & Then
        mockMvc.perform(get("/v1/users")
                .with(user(new PrincipalUser(supervisorUser)))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getUsersByOrganization_ShouldReturnForbidden_WhenInspectorRequests() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users")
                .with(user(new PrincipalUser(inspectorUser))))
                .andExpect(status().isForbidden());
    }

    // GET USER BY ID TESTS

    @Test
    void getUserById_ShouldReturnUser_WhenAdminRequests() throws Exception {
        // Given
        given(userService.findUserById(2L, adminUser))
            .willReturn(userDto);

        // When & Then
        mockMvc.perform(get("/v1/users/2")
                .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Given
        given(userService.findUserById(999L, adminUser))
            .willThrow(new ResourceNotFoundException("User not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/v1/users/999")
                .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    // UPDATE USER TESTS

    @Test
    void updateUser_ShouldUpdateUser_WhenAdminMakesValidRequest() throws Exception {
        // Given
        given(userService.updateUser(eq(2L), any(UpdateUserRequest.class), eq(adminUser)))
            .willReturn(userDto);

        // When & Then
        mockMvc.perform(put("/v1/users/2")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void updateUser_ShouldReturnForbidden_WhenNonAdminTriesToUpdate() throws Exception {
        // When & Then
        mockMvc.perform(put("/v1/users/2")
                .with(user(new PrincipalUser(supervisorUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // DELETE USER TESTS

    @Test
    void deleteUser_ShouldDeleteUser_WhenAdminMakesValidRequest() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(2L, adminUser);

        // When & Then
        mockMvc.perform(delete("/v1/users/2")
                .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenNonAdminTriesToDelete() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/users/2")
                .with(user(new PrincipalUser(supervisorUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_ShouldReturnConflict_WhenTryingToDeleteSelf() throws Exception {
        // Given
        willThrow(new ConflictException("You cannot delete your own account"))
            .given(userService).deleteUser(1L, adminUser);

        // When & Then
        mockMvc.perform(delete("/v1/users/1")
                .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You cannot delete your own account"));
    }

    // GET CURRENT USER PROFILE TESTS

    @Test
    void getCurrentUserProfile_ShouldReturnProfile_WhenAnyAuthenticatedUser() throws Exception {
        // Given
        given(userService.getCurrentUserProfile(adminUser))
            .willReturn(userProfileDto);

        // When & Then
        mockMvc.perform(get("/v1/users/me")
                .with(user(new PrincipalUser(adminUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Admin User"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void getCurrentUserProfile_ShouldReturnProfile_WhenInspectorRequests() throws Exception {
        // Given
        UserProfileDto inspectorProfile = new UserProfileDto(
            3L, "Inspector User", "inspector@test.com", "INSPECTOR", "Test Organization", null, LocalDateTime.now()
        );
        given(userService.getCurrentUserProfile(inspectorUser))
            .willReturn(inspectorProfile);

        // When & Then
        mockMvc.perform(get("/v1/users/me")
                .with(user(new PrincipalUser(inspectorUser))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.role").value("INSPECTOR"));
    }

    // CHANGE PASSWORD TESTS

    @Test
    void changePassword_ShouldChangePassword_WhenValidRequest() throws Exception {
        // Given
        doNothing().when(userService).changePassword(any(ChangePasswordRequest.class), eq(adminUser));

        // When & Then
        mockMvc.perform(put("/v1/users/me/password")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_ShouldReturnBadRequest_WhenCurrentPasswordIncorrect() throws Exception {
        // Given
        willThrow(new IllegalArgumentException("Current password is incorrect"))
            .given(userService).changePassword(any(ChangePasswordRequest.class), eq(adminUser));

        // When & Then
        mockMvc.perform(put("/v1/users/me/password")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void changePassword_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        ChangePasswordRequest invalidRequest = new ChangePasswordRequest(
            "", // Empty current password
            "123" // Too short new password
        );

        // When & Then
        mockMvc.perform(put("/v1/users/me/password")
                .with(user(new PrincipalUser(adminUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // UNAUTHORIZED TESTS

    @Test
    void createUser_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsersByOrganization_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUserProfile_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }
}