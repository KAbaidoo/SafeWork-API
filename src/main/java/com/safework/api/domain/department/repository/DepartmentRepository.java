package com.safework.api.domain.department.repository;

import com.safework.api.domain.department.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Finds all departments belonging to a specific organization, with pagination.
     *
     * @param organizationId The ID of the organization.
     * @param pageable       The pagination information.
     * @return A Page of departments for the given organization.
     */
    Page<Department> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Finds a department by name within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name of the department.
     * @return An Optional containing the found department, or empty if not found.
     */
    Optional<Department> findByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Finds a department by code within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param code          The code of the department.
     * @return An Optional containing the found department, or empty if not found.
     */
    Optional<Department> findByOrganizationIdAndCode(Long organizationId, String code);

    /**
     * Checks if a department with the given name exists within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name of the department.
     * @return true if a department with the name exists, false otherwise.
     */
    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Checks if a department with the given code exists within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param code          The code of the department.
     * @return true if a department with the code exists, false otherwise.
     */
    boolean existsByOrganizationIdAndCode(Long organizationId, String code);
}