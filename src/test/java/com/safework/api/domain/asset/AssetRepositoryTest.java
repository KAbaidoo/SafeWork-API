package com.safework.api.domain.asset;

import com.safework.api.domain.asset.model.*;
import com.safework.api.domain.department.model.Department;
import com.safework.api.domain.location.model.Location;
import com.safework.api.domain.maintenance.model.FrequencyUnit;
import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.supplier.model.Supplier;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class AssetRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    private Organization organization;
    private AssetType assetType;
    private User user;
    private Department department;
    private Location location;
    private Supplier supplier;
    private MaintenanceSchedule maintenanceSchedule;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setName("Test Organization");
        entityManager.persist(organization);

        assetType = new AssetType();
        assetType.setName("Equipment");
        assetType.setOrganization(organization);
        entityManager.persist(assetType);

        department = new Department();
        department.setName("Operations");
        department.setOrganization(organization);
        entityManager.persist(department);

        user = new User();
        user.setEmail("asset.user@example.com");
        user.setName("Asset User");
        user.setPassword("password");
        user.setRole(UserRole.INSPECTOR);
        user.setOrganization(organization);
        user.setDepartment(department);
        entityManager.persist(user);

        location = new Location();
        location.setName("Warehouse A");
        location.setOrganization(organization);
        location.setAddress("123 Main St");
        location.setCity("TestCity");
        location.setCountry("TestCountry");
        entityManager.persist(location);

        supplier = new Supplier();
        supplier.setName("Test Supplier");
        supplier.setOrganization(organization);
        supplier.setContactPerson("John Supplier");
        supplier.setEmail("supplier@example.com");
        supplier.setPhoneNumber("+1234567890");
        entityManager.persist(supplier);

        maintenanceSchedule = new MaintenanceSchedule();
        maintenanceSchedule.setName("Quarterly Maintenance");
        maintenanceSchedule.setOrganization(organization);
        maintenanceSchedule.setFrequencyInterval(3);
        maintenanceSchedule.setFrequencyUnit(FrequencyUnit.MONTH);
        entityManager.persist(maintenanceSchedule);

        entityManager.flush();
    }

    @Test
    void testCreateAndRetrieveAsset() {
        Asset asset = new Asset();
        asset.setAssetTag("ASSET-TEST-001");
        asset.setName("Test Equipment");
        asset.setQrCodeId("QR-001");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);

        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        Asset loaded = entityManager.find(Asset.class, asset.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getAssetTag()).isEqualTo("ASSET-TEST-001");
        assertThat(loaded.getName()).isEqualTo("Test Equipment");
        assertThat(loaded.getQrCodeId()).isEqualTo("QR-001");
        assertThat(loaded.getStatus()).isEqualTo(AssetStatus.ACTIVE);
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
    }

    @Test
    void testAssetWithAllRelationships() {
        Asset asset = new Asset();
        asset.setAssetTag("ASSET-FULL-001");
        asset.setName("Fully Configured Asset");
        asset.setQrCodeId("QR-FULL-001");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setDepartment(department);
        asset.setAssignedTo(user);
        asset.setLocation(location);
        asset.setSupplier(supplier);
        asset.setMaintenanceSchedule(maintenanceSchedule);
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setComplianceStatus(ComplianceStatus.COMPLIANT);

        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        Asset loaded = entityManager.find(Asset.class, asset.getId());
        assertThat(loaded.getDepartment().getName()).isEqualTo("Operations");
        assertThat(loaded.getAssignedTo().getEmail()).isEqualTo("asset.user@example.com");
        assertThat(loaded.getLocation().getName()).isEqualTo("Warehouse A");
        assertThat(loaded.getSupplier().getName()).isEqualTo("Test Supplier");
        assertThat(loaded.getMaintenanceSchedule().getName()).isEqualTo("Quarterly Maintenance");
        assertThat(loaded.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
    }

    @Test
    void testAssetUniqueConstraints() {
        Asset asset1 = new Asset();
        asset1.setAssetTag("UNIQUE-TAG");
        asset1.setName("Asset 1");
        asset1.setOrganization(organization);
        asset1.setAssetType(assetType);
        asset1.setStatus(AssetStatus.ACTIVE);
        entityManager.persist(asset1);
        entityManager.flush();

        Asset asset2 = new Asset();
        asset2.setAssetTag("UNIQUE-TAG");
        asset2.setName("Asset 2");
        asset2.setOrganization(organization);
        asset2.setAssetType(assetType);
        asset2.setStatus(AssetStatus.ACTIVE);

        assertThatThrownBy(() -> {
            entityManager.persist(asset2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void testAssetQrCodeUniqueConstraint() {
        Asset asset1 = new Asset();
        asset1.setAssetTag("TAG-001");
        asset1.setName("Asset 1");
        asset1.setQrCodeId("QR-UNIQUE");
        asset1.setOrganization(organization);
        asset1.setAssetType(assetType);
        asset1.setStatus(AssetStatus.ACTIVE);
        entityManager.persist(asset1);
        entityManager.flush();

        Asset asset2 = new Asset();
        asset2.setAssetTag("TAG-002");
        asset2.setName("Asset 2");
        asset2.setQrCodeId("QR-UNIQUE");
        asset2.setOrganization(organization);
        asset2.setAssetType(assetType);
        asset2.setStatus(AssetStatus.ACTIVE);

        assertThatThrownBy(() -> {
            entityManager.persist(asset2);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void testAssetFinancialFields() {
        Asset asset = new Asset();
        asset.setAssetTag("FINANCIAL-001");
        asset.setName("Financial Asset");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setPurchaseDate(LocalDate.of(2024, 1, 15));
        asset.setPurchaseCost(new BigDecimal("12500.50"));
        asset.setWarrantyExpiryDate(LocalDate.of(2026, 1, 15));

        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        Asset loaded = entityManager.find(Asset.class, asset.getId());
        assertThat(loaded.getPurchaseDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(loaded.getPurchaseCost()).isEqualTo(new BigDecimal("12500.50"));
        assertThat(loaded.getWarrantyExpiryDate()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void testAssetLifecycleDates() {
        Asset asset = new Asset();
        asset.setAssetTag("LIFECYCLE-001");
        asset.setName("Lifecycle Asset");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.DECOMMISSIONED);
        asset.setNextServiceDate(LocalDate.of(2024, 6, 1));
        asset.setDisposalDate(LocalDate.of(2024, 12, 31));

        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        Asset loaded = entityManager.find(Asset.class, asset.getId());
        assertThat(loaded.getNextServiceDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(loaded.getDisposalDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void testAssetStatusEnumValues() {
        for (AssetStatus status : AssetStatus.values()) {
            Asset asset = new Asset();
            asset.setAssetTag("STATUS-" + status.name());
            asset.setName("Asset " + status.name());
            asset.setOrganization(organization);
            asset.setAssetType(assetType);
            asset.setStatus(status);

            entityManager.persist(asset);
        }
        entityManager.flush();
        entityManager.clear();

        List<Asset> assets = entityManager
            .createQuery("SELECT a FROM Asset a WHERE a.assetTag LIKE 'STATUS-%'", Asset.class)
            .getResultList();

        assertThat(assets).hasSize(4);
        assertThat(assets).extracting(Asset::getStatus)
            .containsExactlyInAnyOrder(
                AssetStatus.ACTIVE,
                AssetStatus.INACTIVE,
                AssetStatus.UNDER_MAINTENANCE,
                AssetStatus.DECOMMISSIONED
            );
    }

    @Test
    void testAssetComplianceStatusEnum() {
        Asset compliant = new Asset();
        compliant.setAssetTag("COMP-001");
        compliant.setName("Compliant Asset");
        compliant.setOrganization(organization);
        compliant.setAssetType(assetType);
        compliant.setStatus(AssetStatus.ACTIVE);
        compliant.setComplianceStatus(ComplianceStatus.COMPLIANT);

        Asset nonCompliant = new Asset();
        nonCompliant.setAssetTag("COMP-002");
        nonCompliant.setName("Non-Compliant Asset");
        nonCompliant.setOrganization(organization);
        nonCompliant.setAssetType(assetType);
        nonCompliant.setStatus(AssetStatus.ACTIVE);
        nonCompliant.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);

        Asset pending = new Asset();
        pending.setAssetTag("COMP-003");
        pending.setName("Pending Asset");
        pending.setOrganization(organization);
        pending.setAssetType(assetType);
        pending.setStatus(AssetStatus.ACTIVE);
        pending.setComplianceStatus(ComplianceStatus.PENDING_INSPECTION);

        entityManager.persist(compliant);
        entityManager.persist(nonCompliant);
        entityManager.persist(pending);
        entityManager.flush();
        entityManager.clear();

        Asset loadedCompliant = entityManager.find(Asset.class, compliant.getId());
        Asset loadedNonCompliant = entityManager.find(Asset.class, nonCompliant.getId());
        Asset loadedPending = entityManager.find(Asset.class, pending.getId());

        assertThat(loadedCompliant.getComplianceStatus()).isEqualTo(ComplianceStatus.COMPLIANT);
        assertThat(loadedNonCompliant.getComplianceStatus()).isEqualTo(ComplianceStatus.NON_COMPLIANT);
        assertThat(loadedPending.getComplianceStatus()).isEqualTo(ComplianceStatus.PENDING_INSPECTION);
    }

    @Test
    void testAssetVersionForOptimisticLocking() {
        Asset asset = new Asset();
        asset.setAssetTag("VERSION-001");
        asset.setName("Version Test Asset");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);

        entityManager.persist(asset);
        entityManager.flush();
        
        assertThat(asset.getVersion()).isEqualTo(0);

        asset.setName("Updated Name");
        entityManager.flush();
        
        assertThat(asset.getVersion()).isEqualTo(1);

        asset.setStatus(AssetStatus.INACTIVE);
        entityManager.flush();
        
        assertThat(asset.getVersion()).isEqualTo(2);
    }

    @Test
    void testAssetJsonCustomAttributes() {
        Asset asset = new Asset();
        asset.setAssetTag("JSON-001");
        asset.setName("JSON Asset");
        asset.setOrganization(organization);
        asset.setAssetType(assetType);
        asset.setStatus(AssetStatus.ACTIVE);

        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("manufacturer", "ACME Corp");
        customAttributes.put("serialNumber", "SN-123456");
        customAttributes.put("specifications", Map.of(
            "power", "220V",
            "weight", 50.5,
            "dimensions", Map.of("length", 100, "width", 50, "height", 75)
        ));
        customAttributes.put("features", List.of("Feature A", "Feature B", "Feature C"));

        asset.setCustomAttributes(customAttributes);

        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        Asset loaded = entityManager.find(Asset.class, asset.getId());
        assertThat(loaded.getCustomAttributes()).isNotNull();
        assertThat(loaded.getCustomAttributes().get("manufacturer")).isEqualTo("ACME Corp");
        assertThat(loaded.getCustomAttributes().get("serialNumber")).isEqualTo("SN-123456");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> specs = (Map<String, Object>) loaded.getCustomAttributes().get("specifications");
        assertThat(specs.get("power")).isEqualTo("220V");
        assertThat(specs.get("weight")).isEqualTo(50.5);
        
        @SuppressWarnings("unchecked")
        List<String> features = (List<String>) loaded.getCustomAttributes().get("features");
        assertThat(features).containsExactly("Feature A", "Feature B", "Feature C");
    }

    @Test
    void testAssetRequiredFields() {
        Asset assetMissingTag = new Asset();
        assetMissingTag.setName("No Tag");
        assetMissingTag.setOrganization(organization);
        assetMissingTag.setAssetType(assetType);
        assetMissingTag.setStatus(AssetStatus.ACTIVE);

        assertThatThrownBy(() -> {
            entityManager.persist(assetMissingTag);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        Asset assetMissingName = new Asset();
        assetMissingName.setAssetTag("NO-NAME-001");
        assetMissingName.setOrganization(organization);
        assetMissingName.setAssetType(assetType);
        assetMissingName.setStatus(AssetStatus.ACTIVE);

        assertThatThrownBy(() -> {
            entityManager.persist(assetMissingName);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);

        Asset assetMissingStatus = new Asset();
        assetMissingStatus.setAssetTag("NO-STATUS-001");
        assetMissingStatus.setName("No Status");
        assetMissingStatus.setOrganization(organization);
        assetMissingStatus.setAssetType(assetType);

        assertThatThrownBy(() -> {
            entityManager.persist(assetMissingStatus);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void testFindAssetsByStatus() {
        Asset active1 = new Asset();
        active1.setAssetTag("ACTIVE-001");
        active1.setName("Active 1");
        active1.setOrganization(organization);
        active1.setAssetType(assetType);
        active1.setStatus(AssetStatus.ACTIVE);

        Asset active2 = new Asset();
        active2.setAssetTag("ACTIVE-002");
        active2.setName("Active 2");
        active2.setOrganization(organization);
        active2.setAssetType(assetType);
        active2.setStatus(AssetStatus.ACTIVE);

        Asset inactive = new Asset();
        inactive.setAssetTag("INACTIVE-001");
        inactive.setName("Inactive");
        inactive.setOrganization(organization);
        inactive.setAssetType(assetType);
        inactive.setStatus(AssetStatus.INACTIVE);

        entityManager.persist(active1);
        entityManager.persist(active2);
        entityManager.persist(inactive);
        entityManager.flush();
        entityManager.clear();

        List<Asset> activeAssets = entityManager
            .createQuery("SELECT a FROM Asset a WHERE a.status = :status", Asset.class)
            .setParameter("status", AssetStatus.ACTIVE)
            .getResultList();

        assertThat(activeAssets).hasSize(2);
        assertThat(activeAssets).extracting(Asset::getAssetTag)
            .containsExactlyInAnyOrder("ACTIVE-001", "ACTIVE-002");
    }

    @Test
    void testFindAssetsByDepartment() {
        Department dept2 = new Department();
        dept2.setName("IT");
        dept2.setOrganization(organization);
        entityManager.persist(dept2);

        Asset asset1 = new Asset();
        asset1.setAssetTag("DEPT-001");
        asset1.setName("Operations Asset");
        asset1.setOrganization(organization);
        asset1.setAssetType(assetType);
        asset1.setDepartment(department);
        asset1.setStatus(AssetStatus.ACTIVE);

        Asset asset2 = new Asset();
        asset2.setAssetTag("DEPT-002");
        asset2.setName("IT Asset");
        asset2.setOrganization(organization);
        asset2.setAssetType(assetType);
        asset2.setDepartment(dept2);
        asset2.setStatus(AssetStatus.ACTIVE);

        entityManager.persist(asset1);
        entityManager.persist(asset2);
        entityManager.flush();
        entityManager.clear();

        List<Asset> operationsAssets = entityManager
            .createQuery("SELECT a FROM Asset a WHERE a.department.id = :deptId", Asset.class)
            .setParameter("deptId", department.getId())
            .getResultList();

        assertThat(operationsAssets).hasSize(1);
        assertThat(operationsAssets.get(0).getAssetTag()).isEqualTo("DEPT-001");
    }
}