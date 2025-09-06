package com.safework.api.domain.organization.repository;

import com.safework.api.domain.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * Finds an organization by its unique name.
     * Useful for validation during new company sign-ups.
     *
     * @param name The name of the organization.
     * @return An Optional containing the found organization, or empty if not found.
     */
    Optional<Organization> findByName(String name);
}