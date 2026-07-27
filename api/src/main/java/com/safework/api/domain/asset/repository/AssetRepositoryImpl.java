package com.safework.api.domain.asset.repository;

import com.safework.api.domain.asset.model.Asset;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom repository implementation for advanced PostgreSQL JSONB operations.
 * Uses native queries to leverage PostgreSQL's advanced JSON capabilities.
 */
@Repository
public class AssetRepositoryImpl implements AssetRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Asset> findByComplexJsonCriteria(Long organizationId, Map<String, Object> jsonCriteria, Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM assets WHERE organization_id = :organizationId");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("organizationId", organizationId);
        
        int paramIndex = 0;
        for (Map.Entry<String, Object> criteria : jsonCriteria.entrySet()) {
            String paramName = "jsonValue" + paramIndex++;
            queryBuilder.append(" AND custom_attributes ->> :jsonPath").append(paramIndex - 1)
                       .append(" = :").append(paramName);
            parameters.put("jsonPath" + (paramIndex - 1), criteria.getKey());
            parameters.put(paramName, criteria.getValue().toString());
        }
        
        // Add pagination
        queryBuilder.append(" ORDER BY id LIMIT :limit OFFSET :offset");
        parameters.put("limit", pageable.getPageSize());
        parameters.put("offset", pageable.getOffset());
        
        Query query = entityManager.createNativeQuery(queryBuilder.toString(), Asset.class);
        parameters.forEach(query::setParameter);
        
        @SuppressWarnings("unchecked")
        List<Asset> results = query.getResultList();
        
        // Get total count for pagination
        String countQuery = queryBuilder.toString().replaceFirst("SELECT \\*", "SELECT COUNT(*)");
        countQuery = countQuery.replaceFirst(" ORDER BY.*", "");
        
        Query countQueryObj = entityManager.createNativeQuery(countQuery);
        parameters.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("limit") && !entry.getKey().equals("offset"))
            .forEach(entry -> countQueryObj.setParameter(entry.getKey(), entry.getValue()));
        
        Long total = ((BigInteger) countQueryObj.getSingleResult()).longValue();
        
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public List<Asset> findBySimilarJsonStructure(Long organizationId, String templateJson, Double similarityThreshold) {
        String sql = """
            SELECT * FROM assets 
            WHERE organization_id = :organizationId 
            AND similarity(custom_attributes::text, :templateJson) > :threshold
            ORDER BY similarity(custom_attributes::text, :templateJson) DESC
            """;
        
        Query query = entityManager.createNativeQuery(sql, Asset.class);
        query.setParameter("organizationId", organizationId);
        query.setParameter("templateJson", templateJson);
        query.setParameter("threshold", similarityThreshold);
        
        @SuppressWarnings("unchecked")
        List<Asset> results = query.getResultList();
        return results;
    }

    @Override
    public Map<String, Long> aggregateByJsonAttribute(Long organizationId, String jsonPath) {
        String sql = """
            SELECT custom_attributes ->> :jsonPath as attribute_value, COUNT(*) as count
            FROM assets 
            WHERE organization_id = :organizationId 
            AND custom_attributes ? :jsonPath
            GROUP BY custom_attributes ->> :jsonPath
            ORDER BY count DESC
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("organizationId", organizationId);
        query.setParameter("jsonPath", jsonPath);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        return results.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((BigInteger) row[1]).longValue(),
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));
    }

    @Override
    public List<Asset> findByJsonPath(Long organizationId, String jsonPathExpression, Object expectedValue) {
        String sql = """
            SELECT * FROM assets 
            WHERE organization_id = :organizationId 
            AND custom_attributes #> :jsonPath = :expectedValue::jsonb
            """;
        
        Query query = entityManager.createNativeQuery(sql, Asset.class);
        query.setParameter("organizationId", organizationId);
        query.setParameter("jsonPath", jsonPathExpression);
        query.setParameter("expectedValue", expectedValue.toString());
        
        @SuppressWarnings("unchecked")
        List<Asset> results = query.getResultList();
        return results;
    }

    @Override
    public int updateJsonAttribute(Long assetId, String jsonPath, Object newValue) {
        String sql = """
            UPDATE assets 
            SET custom_attributes = jsonb_set(
                COALESCE(custom_attributes, '{}'), 
                :jsonPathArray, 
                :newValue::jsonb,
                true
            ),
            updated_at = CURRENT_TIMESTAMP
            WHERE id = :assetId
            """;
        
        // Convert JSON path to PostgreSQL array format
        String[] pathParts = jsonPath.split("\\.");
        String jsonPathArray = "{" + String.join(",", pathParts) + "}";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("assetId", assetId);
        query.setParameter("jsonPathArray", jsonPathArray);
        query.setParameter("newValue", "\"" + newValue.toString() + "\"");
        
        return query.executeUpdate();
    }

    @Override
    public List<Asset> findByJsonValueRegex(Long organizationId, String jsonPath, String regexPattern) {
        String sql = """
            SELECT * FROM assets 
            WHERE organization_id = :organizationId 
            AND custom_attributes ->> :jsonPath ~ :regexPattern
            """;
        
        Query query = entityManager.createNativeQuery(sql, Asset.class);
        query.setParameter("organizationId", organizationId);
        query.setParameter("jsonPath", jsonPath);
        query.setParameter("regexPattern", regexPattern);
        
        @SuppressWarnings("unchecked")
        List<Asset> results = query.getResultList();
        return results;
    }

    @Override
    public List<Asset> findByJsonArrayContainsAll(Long organizationId, String arrayPath, String[] requiredValues) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM assets WHERE organization_id = :organizationId");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("organizationId", organizationId);
        
        for (int i = 0; i < requiredValues.length; i++) {
            String paramName = "value" + i;
            sql.append(" AND custom_attributes -> :arrayPath ? :").append(paramName);
            parameters.put(paramName, requiredValues[i]);
        }
        
        Query query = entityManager.createNativeQuery(sql.toString(), Asset.class);
        query.setParameter("arrayPath", arrayPath);
        parameters.forEach(query::setParameter);
        
        @SuppressWarnings("unchecked")
        List<Asset> results = query.getResultList();
        return results;
    }

    @Override
    public Map<String, Double> calculateJsonFieldStatistics(Long organizationId, String numericJsonPath) {
        String sql = """
            SELECT 
                MIN((custom_attributes ->> :jsonPath)::numeric) as min_value,
                MAX((custom_attributes ->> :jsonPath)::numeric) as max_value,
                AVG((custom_attributes ->> :jsonPath)::numeric) as avg_value,
                COUNT(*) as count
            FROM assets 
            WHERE organization_id = :organizationId 
            AND custom_attributes ? :jsonPath
            AND custom_attributes ->> :jsonPath ~ '^[0-9]+\\.?[0-9]*$'
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("organizationId", organizationId);
        query.setParameter("jsonPath", numericJsonPath);
        
        Object[] result = (Object[]) query.getSingleResult();
        
        Map<String, Double> statistics = new HashMap<>();
        statistics.put("min", result[0] != null ? ((BigDecimal) result[0]).doubleValue() : 0.0);
        statistics.put("max", result[1] != null ? ((BigDecimal) result[1]).doubleValue() : 0.0);
        statistics.put("avg", result[2] != null ? ((BigDecimal) result[2]).doubleValue() : 0.0);
        statistics.put("count", result[3] != null ? ((BigInteger) result[3]).doubleValue() : 0.0);
        
        return statistics;
    }
}