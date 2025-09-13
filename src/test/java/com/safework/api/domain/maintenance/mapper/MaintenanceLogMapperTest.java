package com.safework.api.domain.maintenance.mapper;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.maintenance.dto.MaintenanceLogDto;
import com.safework.api.domain.maintenance.dto.MaintenanceLogSummaryDto;
import com.safework.api.domain.maintenance.model.MaintenanceLog;
import com.safework.api.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MaintenanceLogMapper Tests")
class MaintenanceLogMapperTest {

    private MaintenanceLogMapper maintenanceLogMapper;
    private Asset testAsset;
    private User testTechnician;
    private MaintenanceLog testLog;

    @BeforeEach
    void setUp() throws Exception {
        maintenanceLogMapper = new MaintenanceLogMapper();
        setupTestData();
    }

    private void setupTestData() throws Exception {
        // Setup asset
        testAsset = new Asset();
        setEntityId(testAsset, 1L);
        testAsset.setAssetTag("TEST-001");
        testAsset.setName("Test Asset");

        // Setup technician
        testTechnician = new User();
        setEntityId(testTechnician, 2L);
        testTechnician.setName("John Doe");

        // Setup maintenance log
        testLog = new MaintenanceLog();
        setEntityId(testLog, 1L);
        testLog.setAsset(testAsset);
        testLog.setTechnician(testTechnician);
        testLog.setServiceDate(LocalDate.now().minusDays(1));
        testLog.setNotes("Routine maintenance performed");
        testLog.setCost(new BigDecimal("150.00"));
    }

    private void setEntityId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    @DisplayName("Should map MaintenanceLog to MaintenanceLogDto")
    void shouldMapToDto_WhenValidMaintenanceLog() {
        // When
        MaintenanceLogDto result = maintenanceLogMapper.toDto(testLog);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.assetId()).isEqualTo(1L);
        assertThat(result.assetName()).isEqualTo("Test Asset");
        assertThat(result.assetTag()).isEqualTo("TEST-001");
        assertThat(result.technicianId()).isEqualTo(2L);
        assertThat(result.technicianName()).isEqualTo("John Doe");
        assertThat(result.serviceDate()).isEqualTo(LocalDate.now().minusDays(1));
        assertThat(result.notes()).isEqualTo("Routine maintenance performed");
        assertThat(result.cost()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should map MaintenanceLog to MaintenanceLogSummaryDto")
    void shouldMapToSummaryDto_WhenValidMaintenanceLog() {
        // When
        MaintenanceLogSummaryDto result = maintenanceLogMapper.toSummaryDto(testLog);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.assetId()).isEqualTo(1L);
        assertThat(result.assetName()).isEqualTo("Test Asset");
        assertThat(result.assetTag()).isEqualTo("TEST-001");
        assertThat(result.serviceDate()).isEqualTo(LocalDate.now().minusDays(1));
        assertThat(result.cost()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should handle null asset in toDto")
    void shouldHandleNullAsset_InToDto() {
        // Given
        testLog.setAsset(null);

        // When
        MaintenanceLogDto result = maintenanceLogMapper.toDto(testLog);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.assetId()).isNull();
        assertThat(result.assetName()).isNull();
        assertThat(result.assetTag()).isNull();
        assertThat(result.technicianName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should handle null technician in toDto")
    void shouldHandleNullTechnician_InToDto() {
        // Given
        testLog.setTechnician(null);

        // When
        MaintenanceLogDto result = maintenanceLogMapper.toDto(testLog);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.technicianId()).isNull();
        assertThat(result.technicianName()).isNull();
        assertThat(result.assetName()).isEqualTo("Test Asset");
    }

    @Test
    @DisplayName("Should handle null asset in toSummaryDto")
    void shouldHandleNullAsset_InToSummaryDto() {
        // Given
        testLog.setAsset(null);

        // When
        MaintenanceLogSummaryDto result = maintenanceLogMapper.toSummaryDto(testLog);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.assetId()).isNull();
        assertThat(result.assetName()).isNull();
        assertThat(result.assetTag()).isNull();
        assertThat(result.serviceDate()).isEqualTo(LocalDate.now().minusDays(1));
        assertThat(result.cost()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should handle null notes")
    void shouldHandleNullNotes() {
        // Given
        testLog.setNotes(null);

        // When
        MaintenanceLogDto result = maintenanceLogMapper.toDto(testLog);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.notes()).isNull();
        assertThat(result.assetName()).isEqualTo("Test Asset");
    }

    @Test
    @DisplayName("Should handle null cost")
    void shouldHandleNullCost() {
        // Given
        testLog.setCost(null);

        // When
        MaintenanceLogDto dto = maintenanceLogMapper.toDto(testLog);
        MaintenanceLogSummaryDto summaryDto = maintenanceLogMapper.toSummaryDto(testLog);

        // Then
        assertThat(dto.cost()).isNull();
        assertThat(summaryDto.cost()).isNull();
    }

    @Test
    @DisplayName("Should handle zero cost")
    void shouldHandleZeroCost() {
        // Given
        testLog.setCost(BigDecimal.ZERO);

        // When
        MaintenanceLogDto dto = maintenanceLogMapper.toDto(testLog);
        MaintenanceLogSummaryDto summaryDto = maintenanceLogMapper.toSummaryDto(testLog);

        // Then
        assertThat(dto.cost()).isEqualTo(BigDecimal.ZERO);
        assertThat(summaryDto.cost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle large cost values")
    void shouldHandleLargeCostValues() {
        // Given
        BigDecimal largeCost = new BigDecimal("999999.99");
        testLog.setCost(largeCost);

        // When
        MaintenanceLogDto dto = maintenanceLogMapper.toDto(testLog);
        MaintenanceLogSummaryDto summaryDto = maintenanceLogMapper.toSummaryDto(testLog);

        // Then
        assertThat(dto.cost()).isEqualTo(largeCost);
        assertThat(summaryDto.cost()).isEqualTo(largeCost);
    }

    @Test
    @DisplayName("Should format technician name correctly")
    void shouldFormatTechnicianName_WhenValidTechnician() {
        // Given - technician already set up with "John" and "Doe"

        // When
        MaintenanceLogDto result = maintenanceLogMapper.toDto(testLog);

        // Then
        assertThat(result.technicianName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should handle technician with null name")
    void shouldHandleTechnicianWithNullName() {
        // Given
        testTechnician.setName(null);

        // When
        MaintenanceLogDto result = maintenanceLogMapper.toDto(testLog);

        // Then
        assertThat(result.technicianName()).isNull();
    }

    @Test
    @DisplayName("Should handle technician with empty name")
    void shouldHandleTechnicianWithEmptyName() {
        // Given
        testTechnician.setName("");

        // When
        MaintenanceLogDto result = maintenanceLogMapper.toDto(testLog);

        // Then
        assertThat(result.technicianName()).isEmpty();
    }

    @Test
    @DisplayName("Should preserve all data integrity in mapping")
    void shouldPreserveDataIntegrity_WhenMapping() {
        // When
        MaintenanceLogDto dto = maintenanceLogMapper.toDto(testLog);

        // Then - Verify all fields are mapped correctly
        assertThat(dto.id()).isEqualTo(testLog.getId());
        assertThat(dto.serviceDate()).isEqualTo(testLog.getServiceDate());
        assertThat(dto.notes()).isEqualTo(testLog.getNotes());
        assertThat(dto.cost()).isEqualTo(testLog.getCost());
        assertThat(dto.assetId()).isEqualTo(testLog.getAsset().getId());
        assertThat(dto.technicianId()).isEqualTo(testLog.getTechnician().getId());
    }

    @Test
    @DisplayName("Should maintain consistency between dto and summary dto mappings")
    void shouldMaintainConsistency_BetweenDtoAndSummaryDto() {
        // When
        MaintenanceLogDto dto = maintenanceLogMapper.toDto(testLog);
        MaintenanceLogSummaryDto summaryDto = maintenanceLogMapper.toSummaryDto(testLog);

        // Then - Verify common fields are consistent
        assertThat(dto.id()).isEqualTo(summaryDto.id());
        assertThat(dto.assetId()).isEqualTo(summaryDto.assetId());
        assertThat(dto.assetName()).isEqualTo(summaryDto.assetName());
        assertThat(dto.assetTag()).isEqualTo(summaryDto.assetTag());
        assertThat(dto.serviceDate()).isEqualTo(summaryDto.serviceDate());
        assertThat(dto.cost()).isEqualTo(summaryDto.cost());
    }
}