package com.safework.api.domain.maintenance.repository;

import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    /**
     * Finds all maintenance schedules belonging to a specific organization, with pagination.
     *
     * @param organizationId The ID of the organization.
     * @param pageable       The pagination information.
     * @return A Page of maintenance schedules for the given organization.
     */
    Page<MaintenanceSchedule> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Finds a maintenance schedule by name within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name of the maintenance schedule.
     * @return An Optional containing the found schedule, or empty if not found.
     */
    Optional<MaintenanceSchedule> findByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Checks if a maintenance schedule with the given name exists within a specific organization.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name of the maintenance schedule.
     * @return true if a schedule with the name exists, false otherwise.
     */
    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    /**
     * Finds all maintenance schedules belonging to a specific organization by name containing search term, with pagination.
     *
     * @param organizationId The ID of the organization.
     * @param name          The name search term.
     * @param pageable      The pagination information.
     * @return A Page of schedules matching the name search for the given organization.
     */
    Page<MaintenanceSchedule> findByOrganizationIdAndNameContainingIgnoreCase(Long organizationId, String name, Pageable pageable);
}