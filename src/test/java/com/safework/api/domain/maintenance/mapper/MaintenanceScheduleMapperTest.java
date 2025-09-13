package com.safework.api.domain.maintenance.mapper;

import com.safework.api.domain.maintenance.dto.MaintenanceScheduleDto;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleSummaryDto;
import com.safework.api.domain.maintenance.model.FrequencyUnit;
import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import com.safework.api.domain.organization.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MaintenanceScheduleMapper Tests")
class MaintenanceScheduleMapperTest {

    private MaintenanceScheduleMapper maintenanceScheduleMapper;
    private Organization testOrganization;
    private MaintenanceSchedule testSchedule;

    @BeforeEach
    void setUp() throws Exception {
        maintenanceScheduleMapper = new MaintenanceScheduleMapper();
        setupTestData();
    }

    private void setupTestData() throws Exception {
        // Setup organization
        testOrganization = new Organization();
        setEntityId(testOrganization, 1L);
        testOrganization.setName("Test Organization");

        // Setup schedule
        testSchedule = new MaintenanceSchedule();
        setEntityId(testSchedule, 1L);
        testSchedule.setName("Annual Fire Extinguisher Check");
        testSchedule.setFrequencyInterval(12);
        testSchedule.setFrequencyUnit(FrequencyUnit.MONTH);
        testSchedule.setOrganization(testOrganization);
    }

    private void setEntityId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    @DisplayName("Should map MaintenanceSchedule to MaintenanceScheduleDto")
    void shouldMapToDto_WhenValidMaintenanceSchedule() {
        // When
        MaintenanceScheduleDto result = maintenanceScheduleMapper.toDto(testSchedule);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Annual Fire Extinguisher Check");
        assertThat(result.frequencyInterval()).isEqualTo(12);
        assertThat(result.frequencyUnit()).isEqualTo(FrequencyUnit.MONTH);
        assertThat(result.organizationId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should map MaintenanceSchedule to MaintenanceScheduleSummaryDto")
    void shouldMapToSummaryDto_WhenValidMaintenanceSchedule() {
        // When
        MaintenanceScheduleSummaryDto result = maintenanceScheduleMapper.toSummaryDto(testSchedule);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Annual Fire Extinguisher Check");
        assertThat(result.frequencyInterval()).isEqualTo(12);
        assertThat(result.frequencyUnit()).isEqualTo(FrequencyUnit.MONTH);
    }

    @Test
    @DisplayName("Should handle null organization in toDto")
    void shouldHandleNullOrganization_InToDto() {
        // Given
        testSchedule.setOrganization(null);

        // When
        MaintenanceScheduleDto result = maintenanceScheduleMapper.toDto(testSchedule);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.organizationId()).isNull();
        assertThat(result.name()).isEqualTo("Annual Fire Extinguisher Check");
    }

    @Test
    @DisplayName("Should map all frequency units correctly")
    void shouldMapAllFrequencyUnits_WhenDifferentUnits() {
        // Test all frequency units
        FrequencyUnit[] units = FrequencyUnit.values();
        
        for (FrequencyUnit unit : units) {
            // Given
            testSchedule.setFrequencyUnit(unit);
            testSchedule.setFrequencyInterval(1);

            // When
            MaintenanceScheduleDto dto = maintenanceScheduleMapper.toDto(testSchedule);
            MaintenanceScheduleSummaryDto summaryDto = maintenanceScheduleMapper.toSummaryDto(testSchedule);

            // Then
            assertThat(dto.frequencyUnit()).isEqualTo(unit);
            assertThat(summaryDto.frequencyUnit()).isEqualTo(unit);
        }
    }

    @Test
    @DisplayName("Should handle different frequency intervals")
    void shouldHandleDifferentFrequencyIntervals() {
        int[] intervals = {1, 3, 6, 12, 24, 365};
        
        for (int interval : intervals) {
            // Given
            testSchedule.setFrequencyInterval(interval);

            // When
            MaintenanceScheduleDto dto = maintenanceScheduleMapper.toDto(testSchedule);
            MaintenanceScheduleSummaryDto summaryDto = maintenanceScheduleMapper.toSummaryDto(testSchedule);

            // Then
            assertThat(dto.frequencyInterval()).isEqualTo(interval);
            assertThat(summaryDto.frequencyInterval()).isEqualTo(interval);
        }
    }

    @Test
    @DisplayName("Should handle empty schedule name")
    void shouldHandleEmptyScheduleName() {
        // Given
        testSchedule.setName("");

        // When
        MaintenanceScheduleDto dto = maintenanceScheduleMapper.toDto(testSchedule);
        MaintenanceScheduleSummaryDto summaryDto = maintenanceScheduleMapper.toSummaryDto(testSchedule);

        // Then
        assertThat(dto.name()).isEmpty();
        assertThat(summaryDto.name()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null schedule name")
    void shouldHandleNullScheduleName() {
        // Given
        testSchedule.setName(null);

        // When
        MaintenanceScheduleDto dto = maintenanceScheduleMapper.toDto(testSchedule);
        MaintenanceScheduleSummaryDto summaryDto = maintenanceScheduleMapper.toSummaryDto(testSchedule);

        // Then
        assertThat(dto.name()).isNull();
        assertThat(summaryDto.name()).isNull();
    }

    @Test
    @DisplayName("Should preserve all data integrity in mapping")
    void shouldPreserveDataIntegrity_WhenMapping() {
        // When
        MaintenanceScheduleDto dto = maintenanceScheduleMapper.toDto(testSchedule);

        // Then - Verify all fields are mapped correctly
        assertThat(dto.id()).isEqualTo(testSchedule.getId());
        assertThat(dto.name()).isEqualTo(testSchedule.getName());
        assertThat(dto.frequencyInterval()).isEqualTo(testSchedule.getFrequencyInterval());
        assertThat(dto.frequencyUnit()).isEqualTo(testSchedule.getFrequencyUnit());
        assertThat(dto.organizationId()).isEqualTo(testSchedule.getOrganization().getId());
    }

    @Test
    @DisplayName("Should maintain consistency between dto and summary dto mappings")
    void shouldMaintainConsistency_BetweenDtoAndSummaryDto() {
        // When
        MaintenanceScheduleDto dto = maintenanceScheduleMapper.toDto(testSchedule);
        MaintenanceScheduleSummaryDto summaryDto = maintenanceScheduleMapper.toSummaryDto(testSchedule);

        // Then - Verify common fields are consistent
        assertThat(dto.id()).isEqualTo(summaryDto.id());
        assertThat(dto.name()).isEqualTo(summaryDto.name());
        assertThat(dto.frequencyInterval()).isEqualTo(summaryDto.frequencyInterval());
        assertThat(dto.frequencyUnit()).isEqualTo(summaryDto.frequencyUnit());
    }
}