package com.safework.api.domain.user.service;

import com.safework.api.domain.department.repository.DepartmentRepository;
import com.safework.api.domain.user.dto.*;
import com.safework.api.domain.user.mapper.UserMapper;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto createUser(CreateUserRequest request, User currentUser) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User with email " + request.email() + " already exists");
        }

        // Validate role
        UserRole role;
        try {
            role = UserRole.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.role());
        }

        User newUser = new User();
        newUser.setOrganization(currentUser.getOrganization());
        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(role);

        // Set department if provided
        if (request.departmentId() != null) {
            var department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
            
            // Ensure department belongs to the same organization
            if (!department.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new AccessDeniedException("Department does not belong to your organization");
            }
            newUser.setDepartment(department);
        }

        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAllByOrganization(Long organizationId, Pageable pageable) {
        Page<User> users = userRepository.findAllByOrganizationId(organizationId, pageable);
        return users.map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto findUserById(Long id, User currentUser) {
        User user = getUserForOrganization(id, currentUser);
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(User currentUser) {
        return userMapper.toProfileDto(currentUser);
    }

    public UserDto updateUser(Long id, UpdateUserRequest request, User currentUser) {
        User userToUpdate = getUserForOrganization(id, currentUser);

        // Check if email is being changed and if it already exists
        if (!userToUpdate.getEmail().equals(request.email()) && 
            userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User with email " + request.email() + " already exists");
        }

        // Validate role
        UserRole role;
        try {
            role = UserRole.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.role());
        }

        userToUpdate.setName(request.name());
        userToUpdate.setEmail(request.email());
        userToUpdate.setRole(role);

        // Update department if provided
        if (request.departmentId() != null) {
            var department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));
            
            // Ensure department belongs to the same organization
            if (!department.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new AccessDeniedException("Department does not belong to your organization");
            }
            userToUpdate.setDepartment(department);
        } else {
            userToUpdate.setDepartment(null);
        }

        User savedUser = userRepository.save(userToUpdate);
        return userMapper.toDto(savedUser);
    }

    public void deleteUser(Long id, User currentUser) {
        User userToDelete = getUserForOrganization(id, currentUser);
        
        // Prevent users from deleting themselves
        if (userToDelete.getId().equals(currentUser.getId())) {
            throw new ConflictException("You cannot delete your own account");
        }

        userRepository.delete(userToDelete);
    }

    public void changePassword(ChangePasswordRequest request, User currentUser) {
        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }

    /**
     * Helper method to fetch a user and verify they belong to the same organization.
     */
    private User getUserForOrganization(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Multi-tenancy security check
        if (!user.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("You do not have permission to access this user");
        }
        
        return user;
    }
}