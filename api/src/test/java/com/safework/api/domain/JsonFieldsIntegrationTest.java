package com.safework.api.domain;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.checklist.model.Checklist;
import com.safework.api.domain.checklist.model.ChecklistStatus;
import com.safework.api.domain.inspection.model.Inspection;
import com.safework.api.domain.inspection.model.InspectionStatus;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for JSON field handling across different databases (H2 for testing, MariaDB for production).
 * Verifies that JSON data can be persisted and retrieved correctly.
 */
@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class JsonFieldsIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    private Organization organization;
    private User user;
    private AssetType assetType;

    @BeforeEach
    void setUp() {
        // Create required entities for relationships
        organization = new Organization();
        organization.setName("Test Organization");
        entityManager.persist(organization);

        user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("hashedpassword");
        user.setRole(UserRole.INSPECTOR);
        user.setOrganization(organization);
        entityManager.persist(user);

        assetType = new AssetType();
        assetType.setName("Equipment");
        assetType.setOrganization(organization);
        entityManager.persist(assetType);

        entityManager.flush();
    }

    @Test
    void testAssetCustomAttributesJsonPersistence() {
        // Given
        Asset asset = new Asset();
        asset.setAssetTag("ASSET-001");
        asset.setName("Test Equipment");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);

        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("manufacturer", "ACME Corp");
        customAttributes.put("model", "X-2000");
        customAttributes.put("specifications", Map.of(
            "weight", "50kg",
            "dimensions", Map.of("length", 100, "width", 50, "height", 75)
        ));
        customAttributes.put("certifications", List.of("ISO9001", "CE"));

        asset.setCustomAttributes(customAttributes);

        // When
        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear(); // Clear cache to force reload from DB

        // Then
        Asset loadedAsset = entityManager.find(Asset.class, asset.getId());
        assertThat(loadedAsset).isNotNull();
        assertThat(loadedAsset.getCustomAttributes()).isNotNull();
        assertThat(loadedAsset.getCustomAttributes().get("manufacturer")).isEqualTo("ACME Corp");
        assertThat(loadedAsset.getCustomAttributes().get("model")).isEqualTo("X-2000");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> specs = (Map<String, Object>) loadedAsset.getCustomAttributes().get("specifications");
        assertThat(specs.get("weight")).isEqualTo("50kg");
        
        @SuppressWarnings("unchecked")
        List<String> certs = (List<String>) loadedAsset.getCustomAttributes().get("certifications");
        assertThat(certs).containsExactly("ISO9001", "CE");
    }

    @Test
    void testChecklistTemplateDataJsonPersistence() {
        // Given
        Checklist checklist = new Checklist();
        checklist.setName("Safety Inspection Checklist");
        checklist.setOrganization(organization);
        checklist.setStatus(ChecklistStatus.ACTIVE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("version", "1.0");
        templateData.put("sections", List.of(
            Map.of(
                "name", "General Safety",
                "items", List.of(
                    Map.of("id", "GS-001", "question", "Are safety signs visible?", "type", "boolean"),
                    Map.of("id", "GS-002", "question", "Emergency exits clear?", "type", "boolean")
                )
            ),
            Map.of(
                "name", "Equipment Check",
                "items", List.of(
                    Map.of("id", "EQ-001", "question", "Equipment condition", "type", "rating", "scale", 5)
                )
            )
        ));

        checklist.setTemplateData(templateData);

        // When
        entityManager.persist(checklist);
        entityManager.flush();
        entityManager.clear();

        // Then
        Checklist loadedChecklist = entityManager.find(Checklist.class, checklist.getId());
        assertThat(loadedChecklist).isNotNull();
        assertThat(loadedChecklist.getTemplateData()).isNotNull();
        assertThat(loadedChecklist.getTemplateData().get("version")).isEqualTo("1.0");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sections = (List<Map<String, Object>>) loadedChecklist.getTemplateData().get("sections");
        assertThat(sections).hasSize(2);
        assertThat(sections.get(0).get("name")).isEqualTo("General Safety");
    }

    @Test
    void testInspectionReportDataJsonPersistence() {
        // Given - Create a checklist first
        Checklist checklist = new Checklist();
        checklist.setName("Test Checklist");
        checklist.setOrganization(organization);
        checklist.setStatus(ChecklistStatus.ACTIVE);
        checklist.setTemplateData(Map.of("version", "1.0"));
        entityManager.persist(checklist);

        // Create an asset
        Asset asset = new Asset();
        asset.setAssetTag("ASSET-002");
        asset.setName("Test Asset");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);
        entityManager.persist(asset);

        // Create inspection
        Inspection inspection = new Inspection();
        inspection.setAsset(asset);
        inspection.setUser(user);
        inspection.setChecklist(checklist);
        inspection.setStatus(InspectionStatus.PASSED);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("inspectionDate", "2024-01-15");
        reportData.put("duration", "45 minutes");
        reportData.put("responses", Map.of(
            "GS-001", true,
            "GS-002", false,
            "EQ-001", 4
        ));
        reportData.put("notes", "Minor issues found with emergency exit signage");
        reportData.put("photos", List.of("photo1.jpg", "photo2.jpg"));

        inspection.setReportData(reportData);

        // When
        entityManager.persist(inspection);
        entityManager.flush();
        entityManager.clear();

        // Then
        Inspection loadedInspection = entityManager.find(Inspection.class, inspection.getId());
        assertThat(loadedInspection).isNotNull();
        assertThat(loadedInspection.getReportData()).isNotNull();
        assertThat(loadedInspection.getReportData().get("duration")).isEqualTo("45 minutes");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responses = (Map<String, Object>) loadedInspection.getReportData().get("responses");
        assertThat(responses.get("GS-001")).isEqualTo(true);
        assertThat(responses.get("EQ-001")).isEqualTo(4);
    }

    @Test
    void testNullJsonFieldsAreAllowed() {
        // Given
        Asset asset = new Asset();
        asset.setAssetTag("ASSET-003");
        asset.setName("Asset without custom attributes");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setCustomAttributes(null); // Explicitly null

        // When
        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        // Then
        Asset loadedAsset = entityManager.find(Asset.class, asset.getId());
        assertThat(loadedAsset).isNotNull();
        assertThat(loadedAsset.getCustomAttributes()).isNull();
    }

    @Test
    void testInvalidJsonDataThrowsException() {
        // Given
        Asset asset = new Asset();
        asset.setAssetTag("ASSET-004");
        asset.setName("Asset with invalid JSON");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);

        // Create a map with a cyclic reference (which cannot be serialized to JSON)
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("self", invalidData); // Cyclic reference

        // When/Then
        assertThatThrownBy(() -> asset.setCustomAttributes(invalidData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid JSON data");
    }

    @Test
    void testComplexNestedJsonStructures() {
        // Given
        Asset asset = new Asset();
        asset.setAssetTag("ASSET-005");
        asset.setName("Asset with complex JSON");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);

        Map<String, Object> complexData = new HashMap<>();
        complexData.put("metadata", Map.of(
            "created", "2024-01-01",
            "tags", List.of("critical", "production"),
            "properties", Map.of(
                "nested", Map.of(
                    "deeply", Map.of(
                        "value", "Found me!",
                        "numbers", List.of(1, 2, 3, 4, 5)
                    )
                )
            )
        ));

        asset.setCustomAttributes(complexData);

        // When
        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        // Then
        Asset loadedAsset = entityManager.find(Asset.class, asset.getId());
        assertThat(loadedAsset).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) loadedAsset.getCustomAttributes().get("metadata");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) metadata.get("properties");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) properties.get("nested");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> deeply = (Map<String, Object>) nested.get("deeply");
        
        assertThat(deeply.get("value")).isEqualTo("Found me!");
        
        @SuppressWarnings("unchecked")
        List<Integer> numbers = (List<Integer>) deeply.get("numbers");
        assertThat(numbers).containsExactly(1, 2, 3, 4, 5);
    }
}