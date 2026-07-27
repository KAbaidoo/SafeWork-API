package com.safework.api.domain.maintenance.repository;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.model.AssetStatus;
import com.safework.api.domain.asset.model.AssetType;
import com.safework.api.domain.maintenance.model.MaintenanceLog;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MaintenanceLogRepository Tests")
class MaintenanceLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    private Organization testOrganization;
    private Organization anotherOrganization;
    private AssetType testAssetType;
    private Asset testAsset;
    private Asset anotherAsset;
    private User testTechnician;
    private User anotherTechnician;
    private MaintenanceLog testLog;
    private MaintenanceLog anotherLog;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // Create organizations
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setPhone("+1234567890");
        testOrganization = entityManager.persistAndFlush(testOrganization);

        anotherOrganization = new Organization();
        anotherOrganization.setName("Another Organization");
        anotherOrganization.setPhone("+0987654321");
        anotherOrganization = entityManager.persistAndFlush(anotherOrganization);

        // Create asset type
        testAssetType = new AssetType();
        testAssetType.setName("Test Equipment");
        testAssetType.setOrganization(testOrganization);
        testAssetType = entityManager.persistAndFlush(testAssetType);

        // Create assets
        testAsset = new Asset();
        testAsset.setAssetTag("TEST-001");
        testAsset.setName("Test Asset");
        testAsset.setStatus(AssetStatus.ACTIVE);
        testAsset.setOrganization(testOrganization);
        testAsset.setAssetType(testAssetType);
        testAsset = entityManager.persistAndFlush(testAsset);

        anotherAsset = new Asset();
        anotherAsset.setAssetTag("TEST-002");
        anotherAsset.setName("Another Asset");
        anotherAsset.setStatus(AssetStatus.ACTIVE);
        anotherAsset.setOrganization(anotherOrganization);
        anotherAsset.setAssetType(testAssetType);
        anotherAsset = entityManager.persistAndFlush(anotherAsset);

        // Create technicians
        testTechnician = new User();
        testTechnician.setEmail("tech1@test.com");
        testTechnician.setName("John Doe");
        testTechnician.setPassword("password");
        testTechnician.setRole(UserRole.INSPECTOR);
        testTechnician.setOrganization(testOrganization);
        testTechnician = entityManager.persistAndFlush(testTechnician);

        anotherTechnician = new User();
        anotherTechnician.setEmail("tech2@test.com");
        anotherTechnician.setName("Jane Smith");
        anotherTechnician.setPassword("password");
        anotherTechnician.setRole(UserRole.INSPECTOR);
        anotherTechnician.setOrganization(testOrganization);
        anotherTechnician = entityManager.persistAndFlush(anotherTechnician);

        // Create maintenance logs
        testLog = new MaintenanceLog();
        testLog.setAsset(testAsset);
        testLog.setTechnician(testTechnician);
        testLog.setServiceDate(LocalDate.now().minusDays(5));
        testLog.setNotes("Routine maintenance performed");
        testLog.setCost(new BigDecimal("150.00"));
        testLog = entityManager.persistAndFlush(testLog);

        anotherLog = new MaintenanceLog();
        anotherLog.setAsset(testAsset);
        anotherLog.setTechnician(anotherTechnician);
        anotherLog.setServiceDate(LocalDate.now().minusDays(10));
        anotherLog.setNotes("Emergency repair");
        anotherLog.setCost(new BigDecimal("300.00"));
        anotherLog = entityManager.persistAndFlush(anotherLog);
    }

    @Test
    @DisplayName("Should find logs by asset ID ordered by service date descending")
    void shouldFindByAssetIdOrderByServiceDateDesc() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByAssetIdOrderByServiceDateDesc(
                testAsset.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        // Should be ordered by service date descending (most recent first)
        assertThat(result.getContent().get(0).getServiceDate()).isAfter(result.getContent().get(1).getServiceDate());
        assertThat(result.getContent().get(0).getNotes()).isEqualTo("Routine maintenance performed");
        assertThat(result.getContent().get(1).getNotes()).isEqualTo("Emergency repair");
    }

    @Test
    @DisplayName("Should find logs by technician ID ordered by service date descending")
    void shouldFindByTechnicianIdOrderByServiceDateDesc() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByTechnicianIdOrderByServiceDateDesc(
                testTechnician.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTechnician().getId()).isEqualTo(testTechnician.getId());
        assertThat(result.getContent().get(0).getNotes()).isEqualTo("Routine maintenance performed");
    }

    @Test
    @DisplayName("Should find logs by organization ID ordered by service date descending")
    void shouldFindByOrganizationIdOrderByServiceDateDesc() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByOrganizationIdOrderByServiceDateDesc(
                testOrganization.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        // Verify all logs belong to the test organization
        assertThat(result.getContent()).allMatch(log -> 
                log.getAsset().getOrganization().getId().equals(testOrganization.getId()));
        // Should be ordered by service date descending
        assertThat(result.getContent().get(0).getServiceDate()).isAfter(result.getContent().get(1).getServiceDate());
    }

    @Test
    @DisplayName("Should find logs by organization ID and date range")
    void shouldFindByOrganizationIdAndServiceDateBetween() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(3);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByOrganizationIdAndServiceDateBetween(
                testOrganization.getId(), startDate, endDate, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNotes()).isEqualTo("Routine maintenance performed");
        assertThat(result.getContent().get(0).getServiceDate()).isBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should find most recent log by asset ID")
    void shouldFindMostRecentByAssetId() {
        // When
        List<MaintenanceLog> result = maintenanceLogRepository.findMostRecentByAssetId(testAsset.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotes()).isEqualTo("Routine maintenance performed");
        assertThat(result.get(0).getServiceDate()).isEqualTo(LocalDate.now().minusDays(5));
    }

    @Test
    @DisplayName("Should return empty list when no logs exist for asset")
    void shouldReturnEmptyList_WhenNoLogsExistForAsset() {
        // Given
        Asset assetWithoutLogs = new Asset();
        assetWithoutLogs.setAssetTag("NO-LOGS");
        assetWithoutLogs.setName("Asset Without Logs");
        assetWithoutLogs.setStatus(AssetStatus.ACTIVE);
        assetWithoutLogs.setOrganization(testOrganization);
        assetWithoutLogs.setAssetType(testAssetType);
        assetWithoutLogs = entityManager.persistAndFlush(assetWithoutLogs);

        // When
        List<MaintenanceLog> result = maintenanceLogRepository.findMostRecentByAssetId(assetWithoutLogs.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePagination_WhenMultipleLogs() {
        // Given - Create additional logs
        for (int i = 0; i < 5; i++) {
            MaintenanceLog log = new MaintenanceLog();
            log.setAsset(testAsset);
            log.setTechnician(testTechnician);
            log.setServiceDate(LocalDate.now().minusDays(i + 15));
            log.setNotes("Additional log " + i);
            log.setCost(new BigDecimal("100.00"));
            entityManager.persistAndFlush(log);
        }

        Pageable pageable = PageRequest.of(0, 3);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByAssetIdOrderByServiceDateDesc(
                testAsset.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(7); // 2 original + 5 new
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should isolate logs between organizations")
    void shouldIsolateLogsBetweenOrganizations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> org1Logs = maintenanceLogRepository.findByOrganizationIdOrderByServiceDateDesc(
                testOrganization.getId(), pageable);
        Page<MaintenanceLog> org2Logs = maintenanceLogRepository.findByOrganizationIdOrderByServiceDateDesc(
                anotherOrganization.getId(), pageable);

        // Then
        assertThat(org1Logs.getContent()).hasSize(2);
        assertThat(org2Logs.getContent()).hasSize(0);

        // Verify no cross-organization contamination
        assertThat(org1Logs.getContent()).allMatch(log -> 
                log.getAsset().getOrganization().getId().equals(testOrganization.getId()));
    }

    @Test
    @DisplayName("Should handle logs without technician")
    void shouldHandleLogsWithoutTechnician() {
        // Given
        MaintenanceLog logWithoutTechnician = new MaintenanceLog();
        logWithoutTechnician.setAsset(testAsset);
        logWithoutTechnician.setTechnician(null);
        logWithoutTechnician.setServiceDate(LocalDate.now().minusDays(1));
        logWithoutTechnician.setNotes("Self-service maintenance");
        logWithoutTechnician.setCost(new BigDecimal("50.00"));
        entityManager.persistAndFlush(logWithoutTechnician);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByAssetIdOrderByServiceDateDesc(
                testAsset.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).anyMatch(log -> log.getTechnician() == null);
    }

    @Test
    @DisplayName("Should handle logs with zero cost")
    void shouldHandleLogsWithZeroCost() {
        // Given
        MaintenanceLog freeLog = new MaintenanceLog();
        freeLog.setAsset(testAsset);
        freeLog.setTechnician(testTechnician);
        freeLog.setServiceDate(LocalDate.now().minusDays(2));
        freeLog.setNotes("Warranty repair");
        freeLog.setCost(BigDecimal.ZERO);
        entityManager.persistAndFlush(freeLog);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByAssetIdOrderByServiceDateDesc(
                testAsset.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).anyMatch(log -> 
                log.getCost().compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    @DisplayName("Should find logs within exact date boundaries")
    void shouldFindLogsWithinExactDateBoundaries() {
        // Given
        LocalDate exactDate = LocalDate.now().minusDays(5);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceLog> result = maintenanceLogRepository.findByOrganizationIdAndServiceDateBetween(
                testOrganization.getId(), exactDate, exactDate, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getServiceDate()).isEqualTo(exactDate);
    }
}