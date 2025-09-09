package com.safework.api.domain.department.repository;

import com.safework.api.domain.department.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}