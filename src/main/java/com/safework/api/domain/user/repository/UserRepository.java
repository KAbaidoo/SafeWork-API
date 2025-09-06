package com.safework.api.domain.user.repository;

import com.safework.api.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * This method is ESSENTIAL for the login process and Spring Security.
     *
     * @param email The user's email address.
     * @return An Optional containing the found user, or empty if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds all users belonging to a specific organization, with pagination.
     * Used by admins to manage their organization's user accounts.
     *
     * @param organizationId The ID of the organization.
     * @param pageable       The pagination information.
     * @return A Page of users for the given organization.
     */
    Page<User> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Checks if a user with the given email already exists.
     * More efficient than findByEmail when you only need to check for existence.
     *
     * @param email The email to check.
     * @return true if a user with the email exists, false otherwise.
     */
    boolean existsByEmail(String email);
}
