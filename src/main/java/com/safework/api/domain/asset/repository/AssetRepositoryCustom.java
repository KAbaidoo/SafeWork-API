package com.safework.api.domain.asset.repository;

import com.safework.api.domain.asset.model.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Custom repository interface for advanced PostgreSQL JSONB operations.
 * Provides complex queries that go beyond standard Spring Data JPA capabilities.
 */
public interface AssetRepositoryCustom {

    /**
     * Execute a complex JSONB query with dynamic criteria.
     * Allows building sophisticated queries with multiple JSON conditions.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonCriteria Map of JSON path to value criteria
     * @param pageable Pagination parameters
     * @return Page of assets matching the complex criteria
     */
    Page<Asset> findByComplexJsonCriteria(Long organizationId, Map<String, Object> jsonCriteria, Pageable pageable);

    /**
     * Find assets with similar JSON structure.
     * Uses PostgreSQL's similarity functions for fuzzy JSON matching.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param templateJson JSON template to match against
     * @param similarityThreshold Similarity threshold (0.0 to 1.0)
     * @return List of assets with similar JSON structure
     */
    List<Asset> findBySimilarJsonStructure(Long organizationId, String templateJson, Double similarityThreshold);

    /**
     * Aggregate assets by JSON attribute values.
     * Useful for reporting and analytics on custom attributes.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to aggregate by
     * @return Map of attribute values to asset counts
     */
    Map<String, Long> aggregateByJsonAttribute(Long organizationId, String jsonPath);

    /**
     * Find assets with JSON path expression.
     * Supports complex JSON path queries using PostgreSQL's #> operator.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPathExpression JSON path expression (e.g., '{specs,engine,power}')
     * @param expectedValue The expected value at the path
     * @return List of assets matching the path expression
     */
    List<Asset> findByJsonPath(Long organizationId, String jsonPathExpression, Object expectedValue);

    /**
     * Update JSON attributes using PostgreSQL's JSONB functions.
     * Allows atomic updates of nested JSON structures.
     *
     * @param assetId The asset ID to update
     * @param jsonPath JSON path to update
     * @param newValue The new value to set
     * @return Number of updated records
     */
    int updateJsonAttribute(Long assetId, String jsonPath, Object newValue);

    /**
     * Search assets with regex patterns in JSON values.
     * Uses PostgreSQL's regex operators with JSON text extraction.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param jsonPath JSON path to search
     * @param regexPattern Regular expression pattern
     * @return List of assets with JSON values matching the regex
     */
    List<Asset> findByJsonValueRegex(Long organizationId, String jsonPath, String regexPattern);

    /**
     * Find assets with JSON arrays containing all specified values.
     * Advanced array containment checking.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param arrayPath JSON path to array field
     * @param requiredValues Array of values that must all be present
     * @return List of assets containing all required values in the JSON array
     */
    List<Asset> findByJsonArrayContainsAll(Long organizationId, String arrayPath, String[] requiredValues);

    /**
     * Calculate JSON field statistics.
     * Provides min, max, avg for numeric JSON fields.
     *
     * @param organizationId The organization ID for multi-tenant filtering
     * @param numericJsonPath JSON path to numeric field
     * @return Map containing statistical data (min, max, avg, count)
     */
    Map<String, Double> calculateJsonFieldStatistics(Long organizationId, String numericJsonPath);
}