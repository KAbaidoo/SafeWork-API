package com.safework.api.domain.asset.repository;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Asset entities with PostgreSQL JSONB support.
 * Extends JpaRepository to provide standard CRUD operations plus advanced JSONB querying.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, AssetRepositoryCustom {

    /**
     * Finds an asset by its unique QR code ID.
     * This is a critical method for the mobile app's scanning feature.
     *
     * @param qrCodeId The QR code identifier to search for.
     * @return An Optional containing the found asset, or empty if not found.
     */
    Optional<Asset> findByQrCodeId(String qrCodeId);

    /**
     * Finds all assets belonging to a specific organization, with pagination support.
     * This is essential for multi-tenancy, ensuring users only see their own company's assets.
     *
     * @param organizationId The ID of the organization.
     * @param pageable       The pagination information (page number, size, and sorting).
     * @return A Page of assets for the given organization.
     */
    Page<Asset> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Finds all assets at a specific location.
     * Used for location-based asset management and transfers.
     *
     * @param locationId The ID of the location.
     * @return A list of assets at the specified location.
     */
    List<Asset> findByLocationId(Long locationId);

    /**
     * Finds assets by status for a specific organization.
     *
     * @param organizationId The organization ID
     * @param status The asset status
     * @param pageable Pagination parameters
     * @return Page of assets with the specified status
     */
    Page<Asset> findByOrganizationIdAndStatus(Long organizationId, AssetStatus status, Pageable pageable);

    /**
     * Find assets with upcoming maintenance (next service date within specified days).
     *
     * @param organizationId The organization ID
     * @param beforeDate The cutoff date for upcoming maintenance
     * @return List of assets requiring maintenance
     */
    @Query("SELECT a FROM Asset a WHERE a.organization.id = :organizationId " +
           "AND a.nextServiceDate IS NOT NULL AND a.nextServiceDate <= :beforeDate " +
           "AND a.status = 'ACTIVE'")
    List<Asset> findUpcomingMaintenance(@Param("organizationId") Long organizationId,
                                       @Param("beforeDate") LocalDate beforeDate);

    // === PostgreSQL JSONB Query Methods for Custom Attributes ===

    /**
     * Find assets where the JSONB custom attributes contain a specific key.
     * Uses PostgreSQL's ? operator for key existence.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonKey The JSON key to search for
     * @return List of assets containing the specified key
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND jsonb_exists(custom_attributes, :jsonKey)", 
           nativeQuery = true)
    List<Asset> findByCustomAttributesContainsKey(@Param("organizationId") Long organizationId,
                                                  @Param("jsonKey") String jsonKey);

    /**
     * Find assets where the JSONB custom attributes contain a specific key-value pair.
     * Uses PostgreSQL's @> operator for containment.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to search (e.g., '{"manufacturer": "CAT"}')
     * @return List of assets containing the specified JSON path
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND custom_attributes @> CAST(:jsonPath AS jsonb)", 
           nativeQuery = true)
    List<Asset> findByCustomAttributesContains(@Param("organizationId") Long organizationId,
                                              @Param("jsonPath") String jsonPath);

    /**
     * Find assets by specific value in JSONB custom attributes using JSON path.
     * Uses PostgreSQL's ->> operator for text extraction.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path (e.g., 'manufacturer' or 'specs->engine')
     * @param value The value to search for
     * @return List of assets with the specified JSON value
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND custom_attributes ->> :jsonPath = :value", 
           nativeQuery = true)
    List<Asset> findByCustomAttributesJsonValue(@Param("organizationId") Long organizationId,
                                               @Param("jsonPath") String jsonPath,
                                               @Param("value") String value);

    /**
     * Find assets by numeric value in JSONB custom attributes.
     * Uses PostgreSQL's ->> operator with CAST for numeric comparison.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to numeric field
     * @param value The numeric value to search for
     * @return List of assets with the specified numeric JSON value
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND (custom_attributes ->> :jsonPath)::numeric = :value", 
           nativeQuery = true)
    List<Asset> findByCustomAttributesNumericValue(@Param("organizationId") Long organizationId,
                                                  @Param("jsonPath") String jsonPath,
                                                  @Param("value") Double value);

    /**
     * Find assets with numeric value in specified range.
     * Useful for searching assets by specifications like weight, power, etc.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to numeric field
     * @param minValue Minimum value (inclusive)
     * @param maxValue Maximum value (inclusive)
     * @return List of assets with numeric value in range
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND (custom_attributes ->> :jsonPath)::numeric BETWEEN :minValue AND :maxValue", 
           nativeQuery = true)
    List<Asset> findByCustomAttributesNumericRange(@Param("organizationId") Long organizationId,
                                                  @Param("jsonPath") String jsonPath,
                                                  @Param("minValue") Double minValue,
                                                  @Param("maxValue") Double maxValue);

    /**
     * Search assets with multiple JSON keys present in custom attributes.
     * Uses PostgreSQL's ?& operator for multiple key existence.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonKeys Array of JSON keys that must all be present
     * @return List of assets containing all specified keys
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND jsonb_exists_all(custom_attributes, ARRAY[:jsonKeys])", 
           nativeQuery = true)
    List<Asset> findByCustomAttributesContainsAllKeys(@Param("organizationId") Long organizationId,
                                                     @Param("jsonKeys") String[] jsonKeys);

    /**
     * Full-text search within JSONB custom attributes.
     * Searches for text across all string values in the JSON structure.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param searchTerm The text to search for
     * @return List of assets containing the search term in their custom attributes
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND to_tsvector('english', custom_attributes::text) @@ plainto_tsquery('english', :searchTerm)", 
           nativeQuery = true)
    List<Asset> searchCustomAttributesFullText(@Param("organizationId") Long organizationId,
                                              @Param("searchTerm") String searchTerm);

    /**
     * Find assets by JSON array contains value.
     * Useful for searching assets with specific tags, certifications, etc.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param arrayPath JSON path to array field
     * @param value The value that should be present in the array
     * @return List of assets containing the value in the specified JSON array
     */
    @Query(value = "SELECT * FROM assets " +
                   "WHERE organization_id = :organizationId " +
                   "AND jsonb_exists(custom_attributes -> :arrayPath, :value)", 
           nativeQuery = true)
    List<Asset> findByCustomAttributesArrayContains(@Param("organizationId") Long organizationId,
                                                   @Param("arrayPath") String arrayPath,
                                                   @Param("value") String value);
}
