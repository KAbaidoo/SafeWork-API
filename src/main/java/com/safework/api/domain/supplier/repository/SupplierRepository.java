package com.safework.api.domain.supplier.repository;

import com.safework.api.domain.supplier.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Finds all suppliers belonging to a specific organization, with pagination.
     *
     * @param organizationId The ID of the organization.
     * @param pageable       The pagination information.
     * @return A Page of suppliers for the given organization.
     */
    Page<Supplier> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Finds a supplier by name within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name of the supplier.
     * @return An Optional containing the found supplier, or empty if not found.
     */
    Optional<Supplier> findByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Checks if a supplier with the given name exists within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name of the supplier.
     * @return true if a supplier with the name exists, false otherwise.
     */
    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Finds all suppliers belonging to a specific organization by email, with pagination.
     *
     * @param organizationId The ID of the organization.
     * @param email         The email to search for.
     * @param pageable      The pagination information.
     * @return A Page of suppliers matching the email for the given organization.
     */
    Page<Supplier> findByOrganizationIdAndEmailContainingIgnoreCase(Long organizationId, String email, Pageable pageable);

    /**
     * Finds all suppliers belonging to a specific organization by name containing search term, with pagination.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name search term.
     * @param pageable      The pagination information.
     * @return A Page of suppliers matching the name search for the given organization.
     */
    Page<Supplier> findByOrganizationIdAndNameContainingIgnoreCase(Long organizationId, String name, Pageable pageable);
}