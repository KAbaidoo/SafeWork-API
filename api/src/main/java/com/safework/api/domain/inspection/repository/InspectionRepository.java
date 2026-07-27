package com.safework.api.domain.inspection.repository;

import com.safework.api.domain.inspection.model.Inspection;
import com.safework.api.domain.inspection.model.InspectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for managing Inspection entities with PostgreSQL JSONB support.
 * Provides standard CRUD operations plus advanced JSONB querying capabilities.
 */
@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    /**
     * Find all inspections for assets belonging to a specific organization.
     * Essential for multi-tenancy data isolation.
     *
     * @param organizationId The organization ID
     * @param pageable Pagination parameters
     * @return Page of inspections for the organization
     */
    @Query("SELECT i FROM Inspection i JOIN i.asset a WHERE a.organization.id = :organizationId")
    Page<Inspection> findAllByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);

    /**
     * Find inspections by asset ID.
     *
     * @param assetId The asset ID
     * @return List of inspections for the asset
     */
    List<Inspection> findByAssetId(Long assetId);

    /**
     * Find inspections by status for a specific organization.
     *
     * @param organizationId The organization ID
     * @param status The inspection status
     * @param pageable Pagination parameters
     * @return Page of inspections with the specified status
     */
    @Query("SELECT i FROM Inspection i JOIN i.asset a WHERE a.organization.id = :organizationId AND i.status = :status")
    Page<Inspection> findByOrganizationIdAndStatus(@Param("organizationId") Long organizationId, 
                                                   @Param("status") InspectionStatus status, 
                                                   Pageable pageable);

    /**
     * Find inspections within a date range for an organization.
     *
     * @param organizationId The organization ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination parameters
     * @return Page of inspections within the date range
     */
    @Query("SELECT i FROM Inspection i JOIN i.asset a WHERE a.organization.id = :organizationId " +
           "AND DATE(i.completedAt) BETWEEN :startDate AND :endDate")
    Page<Inspection> findByOrganizationIdAndDateRange(@Param("organizationId") Long organizationId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate,
                                                      Pageable pageable);

    // === PostgreSQL JSONB Query Methods ===

    /**
     * Find inspections where the JSONB report data contains a specific key.
     * Uses PostgreSQL's ? operator for key existence.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonKey The JSON key to search for
     * @return List of inspections containing the specified key
     */
    @Query(value = "SELECT i.* FROM inspections i " +
                   "JOIN assets a ON i.asset_id = a.id " +
                   "WHERE a.organization_id = :organizationId " +
                   "AND jsonb_exists(i.report_data, :jsonKey)", 
           nativeQuery = true)
    List<Inspection> findByReportDataContainsKey(@Param("organizationId") Long organizationId,
                                                 @Param("jsonKey") String jsonKey);

    /**
     * Find inspections where the JSONB report data contains a specific key-value pair.
     * Uses PostgreSQL's @> operator for containment.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to search (e.g., '{"severity": "high"}')
     * @return List of inspections containing the specified JSON path
     */
    @Query(value = "SELECT i.* FROM inspections i " +
                   "JOIN assets a ON i.asset_id = a.id " +
                   "WHERE a.organization_id = :organizationId " +
                   "AND i.report_data @> CAST(:jsonPath AS jsonb)", 
           nativeQuery = true)
    List<Inspection> findByReportDataContains(@Param("organizationId") Long organizationId,
                                             @Param("jsonPath") String jsonPath);

    /**
     * Find inspections by specific value in JSONB report data using JSON path.
     * Uses PostgreSQL's ->> operator for text extraction.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path (e.g., 'severity' or 'findings->count')
     * @param value The value to search for
     * @return List of inspections with the specified JSON value
     */
    @Query(value = "SELECT i.* FROM inspections i " +
                   "JOIN assets a ON i.asset_id = a.id " +
                   "WHERE a.organization_id = :organizationId " +
                   "AND i.report_data ->> :jsonPath = :value", 
           nativeQuery = true)
    List<Inspection> findByReportDataJsonValue(@Param("organizationId") Long organizationId,
                                              @Param("jsonPath") String jsonPath,
                                              @Param("value") String value);

    /**
     * Search inspections with multiple JSON keys present.
     * Uses PostgreSQL's ?& operator for multiple key existence.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonKeys Array of JSON keys that must all be present
     * @return List of inspections containing all specified keys
     */
    @Query(value = "SELECT i.* FROM inspections i " +
                   "JOIN assets a ON i.asset_id = a.id " +
                   "WHERE a.organization_id = :organizationId " +
                   "AND jsonb_exists_all(i.report_data, ARRAY[:jsonKeys])", 
           nativeQuery = true)
    List<Inspection> findByReportDataContainsAllKeys(@Param("organizationId") Long organizationId,
                                                    @Param("jsonKeys") String[] jsonKeys);

    /**
     * Full-text search within JSONB report data.
     * Searches for text across all string values in the JSON structure.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param searchTerm The text to search for
     * @return List of inspections containing the search term in their report data
     */
    @Query(value = "SELECT i.* FROM inspections i " +
                   "JOIN assets a ON i.asset_id = a.id " +
                   "WHERE a.organization_id = :organizationId " +
                   "AND to_tsvector('english', i.report_data::text) @@ plainto_tsquery('english', :searchTerm)", 
           nativeQuery = true)
    List<Inspection> searchReportDataFullText(@Param("organizationId") Long organizationId,
                                             @Param("searchTerm") String searchTerm);
}