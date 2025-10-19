package com.safework.api.domain.checklist.repository;

import com.safework.api.domain.checklist.model.Checklist;
import com.safework.api.domain.checklist.model.ChecklistStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Checklist entities with PostgreSQL JSONB support.
 * Provides standard CRUD operations plus advanced JSONB querying capabilities for template data.
 */
@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    /**
     * Find all checklists for a specific organization.
     * Essential for multi-tenancy data isolation.
     *
     * @param organizationId The organization ID
     * @param pageable Pagination parameters
     * @return Page of checklists for the organization
     */
    Page<Checklist> findAllByOrganization_Id(Long organizationId, Pageable pageable);

    /**
     * Find checklist by name within an organization.
     * Used for name-based lookups and validation.
     *
     * @param organizationId The organization ID
     * @param name The checklist name
     * @return Optional containing the checklist if found
     */
    Optional<Checklist> findByOrganization_IdAndName(Long organizationId, String name);

    /**
     * Find checklists by status for a specific organization.
     *
     * @param organizationId The organization ID
     * @param status The checklist status
     * @param pageable Pagination parameters
     * @return Page of checklists with the specified status
     */
    Page<Checklist> findByOrganization_IdAndStatus(Long organizationId, ChecklistStatus status, Pageable pageable);

    /**
     * Find active checklists for an organization.
     * Commonly used query for available templates.
     *
     * @param organizationId The organization ID
     * @return List of active checklists
     */
    @Query("SELECT c FROM Checklist c WHERE c.organization.id = :organizationId AND c.status = 'ACTIVE'")
    List<Checklist> findActiveByOrganizationId(@Param("organizationId") Long organizationId);

    // === PostgreSQL JSONB Query Methods for Template Data ===

    /**
     * Find checklists where the JSONB template data contains a specific key.
     * Uses PostgreSQL's ? operator for key existence.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonKey The JSON key to search for (e.g., 'sections', 'version')
     * @return List of checklists containing the specified key
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND jsonb_exists(template_data, :jsonKey)", 
           nativeQuery = true)
    List<Checklist> findByTemplateDataContainsKey(@Param("organizationId") Long organizationId,
                                                  @Param("jsonKey") String jsonKey);

    /**
     * Find checklists where the JSONB template data contains a specific key-value pair.
     * Uses PostgreSQL's @> operator for containment.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to search (e.g., '{"version": "2.0"}' or '{"type": "safety"}')
     * @return List of checklists containing the specified JSON path
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND template_data @> CAST(:jsonPath AS jsonb)", 
           nativeQuery = true)
    List<Checklist> findByTemplateDataContains(@Param("organizationId") Long organizationId,
                                              @Param("jsonPath") String jsonPath);

    /**
     * Find checklists by specific value in JSONB template data using JSON path.
     * Uses PostgreSQL's ->> operator for text extraction.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path (e.g., 'version' or 'metadata->category')
     * @param value The value to search for
     * @return List of checklists with the specified JSON value
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND template_data ->> :jsonPath = :value", 
           nativeQuery = true)
    List<Checklist> findByTemplateDataJsonValue(@Param("organizationId") Long organizationId,
                                               @Param("jsonPath") String jsonPath,
                                               @Param("value") String value);

    /**
     * Find checklists by numeric value in JSONB template data.
     * Uses PostgreSQL's ->> operator with CAST for numeric comparison.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to numeric field
     * @param value The numeric value to search for
     * @return List of checklists with the specified numeric JSON value
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND (template_data ->> :jsonPath)::numeric = :value", 
           nativeQuery = true)
    List<Checklist> findByTemplateDataNumericValue(@Param("organizationId") Long organizationId,
                                                   @Param("jsonPath") String jsonPath,
                                                   @Param("value") Double value);

    /**
     * Search checklists with multiple JSON keys present in template data.
     * Uses PostgreSQL's ?& operator for multiple key existence.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonKeys Array of JSON keys that must all be present
     * @return List of checklists containing all specified keys
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND jsonb_exists_all(template_data, ARRAY[:jsonKeys])", 
           nativeQuery = true)
    List<Checklist> findByTemplateDataContainsAllKeys(@Param("organizationId") Long organizationId,
                                                     @Param("jsonKeys") String[] jsonKeys);

    /**
     * Find checklists where template data contains any of the specified keys.
     * Uses PostgreSQL's ?| operator for "any key exists".
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonKeys Array of JSON keys (checklist matches if any key exists)
     * @return List of checklists containing any of the specified keys
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND jsonb_exists_any(template_data, ARRAY[:jsonKeys])", 
           nativeQuery = true)
    List<Checklist> findByTemplateDataContainsAnyKey(@Param("organizationId") Long organizationId,
                                                    @Param("jsonKeys") String[] jsonKeys);

    /**
     * Full-text search within JSONB template data.
     * Searches for text across all string values in the JSON structure.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param searchTerm The text to search for
     * @return List of checklists containing the search term in their template data
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND to_tsvector('english', template_data::text) @@ plainto_tsquery('english', :searchTerm)", 
           nativeQuery = true)
    List<Checklist> searchTemplateDataFullText(@Param("organizationId") Long organizationId,
                                              @Param("searchTerm") String searchTerm);

    /**
     * Find checklists by nested JSON array length.
     * Useful for finding checklists with specific number of sections or questions.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param arrayPath JSON path to array (e.g., 'sections')
     * @param length Expected array length
     * @return List of checklists with arrays of specified length
     */
    @Query(value = "SELECT * FROM checklists " +
                   "WHERE organization_id = :organizationId " +
                   "AND jsonb_array_length(template_data -> :arrayPath) = :length", 
           nativeQuery = true)
    List<Checklist> findByTemplateDataArrayLength(@Param("organizationId") Long organizationId,
                                                  @Param("arrayPath") String arrayPath,
                                                  @Param("length") Integer length);
}