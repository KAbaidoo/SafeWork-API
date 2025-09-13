package com.safework.api.domain.maintenance.repository;

import com.safework.api.domain.maintenance.model.MaintenanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    /**
     * Finds all maintenance logs for a specific asset, with pagination.
     *
     * @param assetId  The ID of the asset.
     * @param pageable The pagination information.
     * @return A Page of maintenance logs for the given asset.
     */
    Page<MaintenanceLog> findByAssetIdOrderByServiceDateDesc(Long assetId, Pageable pageable);

    /**
     * Finds all maintenance logs performed by a specific technician, with pagination.
     *
     * @param technicianId The ID of the technician (User).
     * @param pageable     The pagination information.
     * @return A Page of maintenance logs for the given technician.
     */
    Page<MaintenanceLog> findByTechnicianIdOrderByServiceDateDesc(Long technicianId, Pageable pageable);

    /**
     * Finds all maintenance logs for assets belonging to a specific organization, with pagination.
     *
     * @param organizationId The ID of the organization.
     * @param pageable       The pagination information.
     * @return A Page of maintenance logs for the organization.
     */
    @Query("SELECT ml FROM MaintenanceLog ml JOIN ml.asset a WHERE a.organization.id = :organizationId ORDER BY ml.serviceDate DESC")
    Page<MaintenanceLog> findByOrganizationIdOrderByServiceDateDesc(@Param("organizationId") Long organizationId, Pageable pageable);

    /**
     * Finds maintenance logs within a specific date range for an organization.
     *
     * @param organizationId The ID of the organization.
     * @param startDate      The start date (inclusive).
     * @param endDate        The end date (inclusive).
     * @param pageable       The pagination information.
     * @return A Page of maintenance logs within the date range.
     */
    @Query("SELECT ml FROM MaintenanceLog ml JOIN ml.asset a WHERE a.organization.id = :organizationId " +
           "AND ml.serviceDate >= :startDate AND ml.serviceDate <= :endDate ORDER BY ml.serviceDate DESC")
    Page<MaintenanceLog> findByOrganizationIdAndServiceDateBetween(@Param("organizationId") Long organizationId, 
                                                                  @Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate,
                                                                  Pageable pageable);

    /**
     * Finds the most recent maintenance log for a specific asset.
     *
     * @param assetId The ID of the asset.
     * @return The most recent maintenance log, or empty if no logs exist.
     */
    @Query("SELECT ml FROM MaintenanceLog ml WHERE ml.asset.id = :assetId ORDER BY ml.serviceDate DESC LIMIT 1")
    List<MaintenanceLog> findMostRecentByAssetId(@Param("assetId") Long assetId);
}