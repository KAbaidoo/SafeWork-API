package com.safework.api.domain.user.controller;

import com.safework.api.domain.user.dto.*;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.service.UserService;
import com.safework.api.security.PrincipalUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user. Requires ADMIN role.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        UserDto newUser = userService.createUser(request, currentUser);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all users for the current user's organization.
     * Requires ADMIN or SUPERVISOR role.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERVISOR')")
    public ResponseEntity<Page<UserDto>> getUsersByOrganization(
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<UserDto> users = userService.findAllByOrganization(currentUser.getOrganization().getId(), pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a single user by their unique ID.
     * Requires ADMIN or SUPERVISOR role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERVISOR')")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        UserDto user = userService.findUserById(id, currentUser);
        return ResponseEntity.ok(user);
    }

    /**
     * Updates an existing user. Requires ADMIN role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        UserDto updatedUser = userService.updateUser(id, request, currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user. Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        userService.deleteUser(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets the current user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        UserProfileDto profile = userService.getCurrentUserProfile(currentUser);
        return ResponseEntity.ok(profile);
    }

    /**
     * Changes the current user's password.
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        userService.changePassword(request, currentUser);
        return ResponseEntity.noContent().build();
    }
}