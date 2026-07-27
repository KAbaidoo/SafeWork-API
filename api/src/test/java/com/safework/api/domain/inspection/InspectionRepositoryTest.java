package com.safework.api.domain.inspection;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.checklist.model.Checklist;
import com.safework.api.domain.checklist.model.ChecklistStatus;
import com.safework.api.domain.inspection.model.Inspection;
import com.safework.api.domain.inspection.model.InspectionStatus;
import com.safework.api.domain.issue.model.Issue;
import com.safework.api.domain.issue.model.IssuePriority;
import com.safework.api.domain.issue.model.IssueStatus;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class InspectionRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    private Organization organization;
    private User inspector;
    private Asset asset;
    private AssetType assetType;
    private Checklist checklist;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setName("Test Organization");
        entityManager.persist(organization);

        inspector = new User();
        inspector.setEmail("inspector@example.com");
        inspector.setName("Inspector User");
        inspector.setPassword("password");
        inspector.setRole(UserRole.INSPECTOR);
        inspector.setOrganization(organization);
        entityManager.persist(inspector);

        assetType = new AssetType();
        assetType.setName("Equipment");
        assetType.setOrganization(organization);
        entityManager.persist(assetType);

        asset = new Asset();
        asset.setAssetTag("ASSET-001");
        asset.setName("Test Equipment");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);
        entityManager.persist(asset);

        checklist = new Checklist();
        checklist.setName("Safety Checklist");
        checklist.setOrganization(organization);
        checklist.setStatus(ChecklistStatus.ACTIVE);
        checklist.setTemplateData(Map.of(
            "version", "1.0",
            "sections", List.of(
                Map.of("name", "General", "items", List.of("Item 1", "Item 2"))
            )
        ));
        entityManager.persist(checklist);

        entityManager.flush();
    }

    @Test
    void testCreateAndRetrieveInspection() {
        Inspection inspection = new Inspection();
        inspection.setAsset(asset);
        inspection.setUser(inspector);
        inspection.setChecklist(checklist);
        inspection.setStatus(InspectionStatus.PASSED);
        inspection.setReportData(Map.of(
            "inspectionDate", "2024-01-15",
            "duration", "30 minutes",
            "score", 95
        ));

        entityManager.persist(inspection);
        entityManager.flush();
        entityManager.clear();

        Inspection loaded = entityManager.find(Inspection.class, inspection.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getStatus()).isEqualTo(InspectionStatus.PASSED);
        assertThat(loaded.getCompletedAt()).isNotNull();
        assertThat(loaded.getReportData().get("duration")).isEqualTo("30 minutes");
        assertThat(loaded.getReportData().get("score")).isEqualTo(95);
    }

    @Test
    void testInspectionRequiredRelationships() {
        // Test missing all required relationships
        Inspection inspection1 = new Inspection();
        inspection1.setStatus(InspectionStatus.PASSED);
        inspection1.setReportData(Map.of("test", "data"));

        assertThatThrownBy(() -> {
            entityManager.persist(inspection1);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        
        entityManager.clear(); // Clear to avoid state issues
        
        // Test missing checklist
        Inspection inspection2 = new Inspection();
        inspection2.setAsset(asset);
        inspection2.setUser(inspector);
        inspection2.setStatus(InspectionStatus.PASSED);
        inspection2.setReportData(Map.of("test", "data"));
        
        assertThatThrownBy(() -> {
            entityManager.persist(inspection2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
        
        entityManager.clear(); // Clear to avoid state issues

        // Test with all required fields
        Inspection inspection3 = new Inspection();
        inspection3.setAsset(asset);
        inspection3.setUser(inspector);
        inspection3.setChecklist(checklist);
        inspection3.setStatus(InspectionStatus.PASSED);
        inspection3.setReportData(Map.of("test", "data"));
        
        entityManager.persist(inspection3);
        entityManager.flush();
        
        assertThat(inspection3.getId()).isNotNull();
    }

    @Test
    void testInspectionStatusEnum() {
        for (InspectionStatus status : InspectionStatus.values()) {
            Inspection inspection = new Inspection();
            inspection.setAsset(asset);
            inspection.setUser(inspector);
            inspection.setChecklist(checklist);
            inspection.setStatus(status);
            inspection.setReportData(Map.of("status", status.toString()));

            entityManager.persist(inspection);
        }
        entityManager.flush();
        entityManager.clear();

        List<Inspection> inspections = entityManager
            .createQuery("SELECT i FROM Inspection i", Inspection.class)
            .getResultList();

        assertThat(inspections).hasSize(2);
        assertThat(inspections).extracting(Inspection::getStatus)
            .containsExactlyInAnyOrder(
                InspectionStatus.PASSED,
                InspectionStatus.COMPLETED_WITH_ISSUES
            );
    }

    @Test
    void testInspectionWithComplexJsonReport() {
        Inspection inspection = new Inspection();
        inspection.setAsset(asset);
        inspection.setUser(inspector);
        inspection.setChecklist(checklist);
        inspection.setStatus(InspectionStatus.PASSED);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("inspectionId", "INS-2024-001");
        reportData.put("timestamp", System.currentTimeMillis());
        reportData.put("location", Map.of(
            "latitude", 40.7128,
            "longitude", -74.0060,
            "address", "New York, NY"
        ));
        reportData.put("checklistResponses", Map.of(
            "section1", Map.of(
                "item1", true,
                "item2", false,
                "item3", "N/A"
            ),
            "section2", Map.of(
                "rating", 4,
                "comments", "Good condition with minor wear"
            )
        ));
        reportData.put("photos", List.of(
            Map.of("url", "photo1.jpg", "caption", "Front view"),
            Map.of("url", "photo2.jpg", "caption", "Serial number")
        ));
        reportData.put("recommendations", List.of(
            "Replace worn parts",
            "Schedule maintenance in 3 months"
        ));

        inspection.setReportData(reportData);

        entityManager.persist(inspection);
        entityManager.flush();
        entityManager.clear();

        Inspection loaded = entityManager.find(Inspection.class, inspection.getId());
        assertThat(loaded.getReportData()).isNotNull();
        assertThat(loaded.getReportData().get("inspectionId")).isEqualTo("INS-2024-001");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> location = (Map<String, Object>) loaded.getReportData().get("location");
        assertThat(location.get("latitude")).isEqualTo(40.7128);
        assertThat(location.get("address")).isEqualTo("New York, NY");

        @SuppressWarnings("unchecked")
        Map<String, Object> responses = (Map<String, Object>) loaded.getReportData().get("checklistResponses");
        assertThat(responses).containsKeys("section1", "section2");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> photos = (List<Map<String, Object>>) loaded.getReportData().get("photos");
        assertThat(photos).hasSize(2);
        assertThat(photos.get(0).get("caption")).isEqualTo("Front view");
    }

    @Test
    void testInspectionWithIssues() {
        Inspection inspection = new Inspection();
        inspection.setAsset(asset);
        inspection.setUser(inspector);
        inspection.setChecklist(checklist);
        inspection.setStatus(InspectionStatus.COMPLETED_WITH_ISSUES);
        inspection.setReportData(Map.of("hasIssues", true));
        entityManager.persist(inspection);

        Issue issue1 = new Issue();
        issue1.setOrganization(organization);
        issue1.setAsset(asset);
        issue1.setInspection(inspection);
        issue1.setReporter(inspector);
        issue1.setDescription("Safety guard missing");
        issue1.setStatus(IssueStatus.OPEN);
        issue1.setPriority(IssuePriority.HIGH);

        Issue issue2 = new Issue();
        issue2.setOrganization(organization);
        issue2.setAsset(asset);
        issue2.setInspection(inspection);
        issue2.setReporter(inspector);
        issue2.setDescription("Minor rust on frame");
        issue2.setStatus(IssueStatus.OPEN);
        issue2.setPriority(IssuePriority.LOW);

        entityManager.persist(issue1);
        entityManager.persist(issue2);
        entityManager.flush();
        entityManager.clear();

        Inspection loaded = entityManager.find(Inspection.class, inspection.getId());
        List<Issue> issues = loaded.getIssues();
        
        assertThat(issues).isNotNull();
        assertThat(issues).hasSize(2);
        assertThat(issues).extracting(Issue::getDescription)
            .containsExactlyInAnyOrder("Safety guard missing", "Minor rust on frame");
    }

    @Test
    void testInspectionImmutability() {
        Inspection inspection = new Inspection();
        inspection.setAsset(asset);
        inspection.setUser(inspector);
        inspection.setChecklist(checklist);
        inspection.setStatus(InspectionStatus.PASSED);
        inspection.setReportData(Map.of("original", "data"));

        entityManager.persist(inspection);
        entityManager.flush();
        
        LocalDateTime originalCompletedAt = inspection.getCompletedAt();
        assertThat(originalCompletedAt).isNotNull();

        inspection.setReportData(Map.of("modified", "data"));
        inspection.setStatus(InspectionStatus.COMPLETED_WITH_ISSUES);
        entityManager.flush();
        entityManager.clear();

        Inspection loaded = entityManager.find(Inspection.class, inspection.getId());
        assertThat(loaded.getStatus()).isEqualTo(InspectionStatus.COMPLETED_WITH_ISSUES);
        assertThat(loaded.getReportData().get("modified")).isEqualTo("data");
        assertThat(loaded.getCompletedAt()).isEqualTo(originalCompletedAt);
    }

    @Test
    void testFindInspectionsByAsset() {
        Asset asset2 = new Asset();
        asset2.setAssetTag("ASSET-002");
        asset2.setName("Second Asset");
        asset2.setOrganization(organization);
        asset2.setAssetType(assetType);
        asset2.setStatus(AssetStatus.ACTIVE);
        entityManager.persist(asset2);

        Inspection inspection1 = new Inspection();
        inspection1.setAsset(asset);
        inspection1.setUser(inspector);
        inspection1.setChecklist(checklist);
        inspection1.setStatus(InspectionStatus.PASSED);
        inspection1.setReportData(Map.of("inspection", 1));

        Inspection inspection2 = new Inspection();
        inspection2.setAsset(asset);
        inspection2.setUser(inspector);
        inspection2.setChecklist(checklist);
        inspection2.setStatus(InspectionStatus.COMPLETED_WITH_ISSUES);
        inspection2.setReportData(Map.of("inspection", 2));

        Inspection inspection3 = new Inspection();
        inspection3.setAsset(asset2);
        inspection3.setUser(inspector);
        inspection3.setChecklist(checklist);
        inspection3.setStatus(InspectionStatus.PASSED);
        inspection3.setReportData(Map.of("inspection", 3));

        entityManager.persist(inspection1);
        entityManager.persist(inspection2);
        entityManager.persist(inspection3);
        entityManager.flush();
        entityManager.clear();

        List<Inspection> assetInspections = entityManager
            .createQuery("SELECT i FROM Inspection i WHERE i.asset.id = :assetId", Inspection.class)
            .setParameter("assetId", asset.getId())
            .getResultList();

        assertThat(assetInspections).hasSize(2);
        assertThat(assetInspections).extracting(i -> i.getReportData().get("inspection"))
            .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void testFindInspectionsByStatus() {
        Inspection passed1 = new Inspection();
        passed1.setAsset(asset);
        passed1.setUser(inspector);
        passed1.setChecklist(checklist);
        passed1.setStatus(InspectionStatus.PASSED);
        passed1.setReportData(Map.of("id", "P1"));

        Inspection passed2 = new Inspection();
        passed2.setAsset(asset);
        passed2.setUser(inspector);
        passed2.setChecklist(checklist);
        passed2.setStatus(InspectionStatus.PASSED);
        passed2.setReportData(Map.of("id", "P2"));

        Inspection failed = new Inspection();
        failed.setAsset(asset);
        failed.setUser(inspector);
        failed.setChecklist(checklist);
        failed.setStatus(InspectionStatus.COMPLETED_WITH_ISSUES);
        failed.setReportData(Map.of("id", "F1"));

        entityManager.persist(passed1);
        entityManager.persist(passed2);
        entityManager.persist(failed);
        entityManager.flush();
        entityManager.clear();

        List<Inspection> passedInspections = entityManager
            .createQuery("SELECT i FROM Inspection i WHERE i.status = :status", Inspection.class)
            .setParameter("status", InspectionStatus.PASSED)
            .getResultList();

        assertThat(passedInspections).hasSize(2);
        assertThat(passedInspections).allMatch(i -> i.getStatus() == InspectionStatus.PASSED);
    }

    @Test
    void testInspectionReportDataNotNull() {
        Inspection inspection = new Inspection();
        inspection.setAsset(asset);
        inspection.setUser(inspector);
        inspection.setChecklist(checklist);
        inspection.setStatus(InspectionStatus.PASSED);

        assertThatThrownBy(() -> {
            entityManager.persist(inspection);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }
}