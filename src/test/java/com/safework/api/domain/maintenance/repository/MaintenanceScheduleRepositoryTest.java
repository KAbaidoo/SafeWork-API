package com.safework.api.domain.maintenance.repository;

import com.safework.api.domain.maintenance.model.FrequencyUnit;
import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import com.safework.api.domain.organization.model.Organization;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MaintenanceScheduleRepository Tests")
class MaintenanceScheduleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    private Organization testOrganization;
    private Organization anotherOrganization;
    private MaintenanceSchedule testSchedule;
    private MaintenanceSchedule anotherSchedule;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setPhone("+1234567890");
        testOrganization = entityManager.persistAndFlush(testOrganization);

        anotherOrganization = new Organization();
        anotherOrganization.setName("Another Organization");
        anotherOrganization.setPhone("+0987654321");
        anotherOrganization = entityManager.persistAndFlush(anotherOrganization);

        testSchedule = new MaintenanceSchedule();
        testSchedule.setOrganization(testOrganization);
        testSchedule.setName("Annual Fire Extinguisher Check");
        testSchedule.setFrequencyInterval(12);
        testSchedule.setFrequencyUnit(FrequencyUnit.MONTH);
        testSchedule = entityManager.persistAndFlush(testSchedule);

        anotherSchedule = new MaintenanceSchedule();
        anotherSchedule.setOrganization(anotherOrganization);
        anotherSchedule.setName("Monthly HVAC Maintenance");
        anotherSchedule.setFrequencyInterval(1);
        anotherSchedule.setFrequencyUnit(FrequencyUnit.MONTH);
        anotherSchedule = entityManager.persistAndFlush(anotherSchedule);
    }

    @Test
    @DisplayName("Should find all schedules by organization ID with pagination")
    void shouldFindAllByOrganizationId_WhenValidOrganization() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceSchedule> result = maintenanceScheduleRepository.findAllByOrganizationId(
                testOrganization.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Annual Fire Extinguisher Check");
        assertThat(result.getContent().get(0).getOrganization().getId()).isEqualTo(testOrganization.getId());
    }

    @Test
    @DisplayName("Should return empty page when organization has no schedules")
    void shouldReturnEmptyPage_WhenOrganizationHasNoSchedules() {
        // Given
        Organization emptyOrganization = new Organization();
        emptyOrganization.setName("Empty Organization");
        emptyOrganization.setPhone("+1111111111");
        emptyOrganization = entityManager.persistAndFlush(emptyOrganization);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceSchedule> result = maintenanceScheduleRepository.findAllByOrganizationId(
                emptyOrganization.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Should find schedule by organization ID and name")
    void shouldFindByOrganizationIdAndName_WhenValidParameters() {
        // When
        Optional<MaintenanceSchedule> result = maintenanceScheduleRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Annual Fire Extinguisher Check");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Annual Fire Extinguisher Check");
        assertThat(result.get().getOrganization().getId()).isEqualTo(testOrganization.getId());
    }

    @Test
    @DisplayName("Should return empty when schedule not found by name")
    void shouldReturnEmpty_WhenScheduleNotFoundByName() {
        // When
        Optional<MaintenanceSchedule> result = maintenanceScheduleRepository.findByOrganizationIdAndName(
                testOrganization.getId(), "Non-existent Schedule");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when searching in wrong organization")
    void shouldReturnEmpty_WhenSearchingInWrongOrganization() {
        // When
        Optional<MaintenanceSchedule> result = maintenanceScheduleRepository.findByOrganizationIdAndName(
                anotherOrganization.getId(), "Annual Fire Extinguisher Check");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if schedule exists by organization ID and name")
    void shouldExistsByOrganizationIdAndName_WhenScheduleExists() {
        // When
        boolean exists = maintenanceScheduleRepository.existsByOrganizationIdAndName(
                testOrganization.getId(), "Annual Fire Extinguisher Check");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when schedule does not exist")
    void shouldReturnFalse_WhenScheduleDoesNotExist() {
        // When
        boolean exists = maintenanceScheduleRepository.existsByOrganizationIdAndName(
                testOrganization.getId(), "Non-existent Schedule");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return false when checking existence in wrong organization")
    void shouldReturnFalse_WhenCheckingExistenceInWrongOrganization() {
        // When
        boolean exists = maintenanceScheduleRepository.existsByOrganizationIdAndName(
                anotherOrganization.getId(), "Annual Fire Extinguisher Check");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find schedules by name containing search term (case insensitive)")
    void shouldFindByNameContaining_WhenValidSearchTerm() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceSchedule> result = maintenanceScheduleRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "fire", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("Fire");
    }

    @Test
    @DisplayName("Should find schedules with case insensitive search")
    void shouldFindByNameContaining_CaseInsensitive() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceSchedule> result = maintenanceScheduleRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "FIRE", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("Fire");
    }

    @Test
    @DisplayName("Should return empty when no schedules match search term")
    void shouldReturnEmpty_WhenNoSchedulesMatchSearchTerm() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceSchedule> result = maintenanceScheduleRepository.findByOrganizationIdAndNameContainingIgnoreCase(
                testOrganization.getId(), "xyz", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePagination_WhenMultipleSchedules() {
        // Given - Create additional schedules
        MaintenanceSchedule schedule1 = new MaintenanceSchedule();
        schedule1.setOrganization(testOrganization);
        schedule1.setName("Weekly Safety Check");
        schedule1.setFrequencyInterval(1);
        schedule1.setFrequencyUnit(FrequencyUnit.WEEK);
        entityManager.persistAndFlush(schedule1);

        MaintenanceSchedule schedule2 = new MaintenanceSchedule();
        schedule2.setOrganization(testOrganization);
        schedule2.setName("Daily Equipment Inspection");
        schedule2.setFrequencyInterval(1);
        schedule2.setFrequencyUnit(FrequencyUnit.DAY);
        entityManager.persistAndFlush(schedule2);

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<MaintenanceSchedule> result = maintenanceScheduleRepository.findAllByOrganizationId(
                testOrganization.getId(), pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should isolate schedules between organizations")
    void shouldIsolateSchedulesBetweenOrganizations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<MaintenanceSchedule> org1Schedules = maintenanceScheduleRepository.findAllByOrganizationId(
                testOrganization.getId(), pageable);
        Page<MaintenanceSchedule> org2Schedules = maintenanceScheduleRepository.findAllByOrganizationId(
                anotherOrganization.getId(), pageable);

        // Then
        assertThat(org1Schedules.getContent()).hasSize(1);
        assertThat(org1Schedules.getContent().get(0).getName()).isEqualTo("Annual Fire Extinguisher Check");

        assertThat(org2Schedules.getContent()).hasSize(1);
        assertThat(org2Schedules.getContent().get(0).getName()).isEqualTo("Monthly HVAC Maintenance");

        // Verify no cross-organization contamination
        assertThat(org1Schedules.getContent()).noneMatch(schedule -> 
                schedule.getOrganization().getId().equals(anotherOrganization.getId()));
        assertThat(org2Schedules.getContent()).noneMatch(schedule -> 
                schedule.getOrganization().getId().equals(testOrganization.getId()));
    }

    @Test
    @DisplayName("Should persist and find schedule with all frequency units")
    void shouldPersistAndFindSchedule_WithAllFrequencyUnits() {
        // Given - Test all frequency units
        FrequencyUnit[] units = FrequencyUnit.values();
        
        for (FrequencyUnit unit : units) {
            MaintenanceSchedule schedule = new MaintenanceSchedule();
            schedule.setOrganization(testOrganization);
            schedule.setName("Test Schedule " + unit.name());
            schedule.setFrequencyInterval(1);
            schedule.setFrequencyUnit(unit);
            entityManager.persistAndFlush(schedule);
        }

        // When
        Page<MaintenanceSchedule> result = maintenanceScheduleRepository.findAllByOrganizationId(
                testOrganization.getId(), PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(units.length);
        
        for (FrequencyUnit unit : units) {
            assertThat(result.getContent()).anyMatch(schedule -> 
                    schedule.getFrequencyUnit() == unit && 
                    schedule.getName().equals("Test Schedule " + unit.name()));
        }
    }
}