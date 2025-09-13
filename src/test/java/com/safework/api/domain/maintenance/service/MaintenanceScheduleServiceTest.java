package com.safework.api.domain.maintenance.service;

import com.safework.api.domain.maintenance.dto.CreateMaintenanceScheduleRequest;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleDto;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleSummaryDto;
import com.safework.api.domain.maintenance.dto.UpdateMaintenanceScheduleRequest;
import com.safework.api.domain.maintenance.mapper.MaintenanceScheduleMapper;
import com.safework.api.domain.maintenance.model.FrequencyUnit;
import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import com.safework.api.domain.maintenance.repository.MaintenanceScheduleRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.exception.ConflictException;
import com.safework.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaintenanceScheduleService Tests")
class MaintenanceScheduleServiceTest {

    @Mock
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    @Mock
    private MaintenanceScheduleMapper maintenanceScheduleMapper;

    @InjectMocks
    private MaintenanceScheduleService maintenanceScheduleService;

    private Organization testOrganization;
    private User testUser;
    private MaintenanceSchedule testSchedule;
    private CreateMaintenanceScheduleRequest createRequest;
    private UpdateMaintenanceScheduleRequest updateRequest;
    private MaintenanceScheduleDto scheduleDto;
    private MaintenanceScheduleSummaryDto scheduleSummaryDto;

    @BeforeEach
    void setUp() throws Exception {
        setupTestData();
    }

    private void setupTestData() throws Exception {
        // Setup organization
        testOrganization = new Organization();
        setEntityId(testOrganization, 1L);
        testOrganization.setName("Test Organization");

        // Setup user
        testUser = new User();
        setEntityId(testUser, 1L);
        testUser.setEmail("test@example.com");
        testUser.setName("John Doe");
        testUser.setRole(UserRole.ADMIN);
        testUser.setOrganization(testOrganization);

        // Setup schedule
        testSchedule = new MaintenanceSchedule();
        setEntityId(testSchedule, 1L);
        testSchedule.setName("Annual Fire Extinguisher Check");
        testSchedule.setFrequencyInterval(12);
        testSchedule.setFrequencyUnit(FrequencyUnit.MONTH);
        testSchedule.setOrganization(testOrganization);

        // Setup DTOs
        createRequest = new CreateMaintenanceScheduleRequest(
                "Annual Fire Extinguisher Check",
                12,
                FrequencyUnit.MONTH
        );

        updateRequest = new UpdateMaintenanceScheduleRequest(
                "Updated Schedule Name",
                6,
                FrequencyUnit.MONTH
        );

        scheduleDto = new MaintenanceScheduleDto(
                1L,
                "Annual Fire Extinguisher Check",
                12,
                FrequencyUnit.MONTH,
                1L
        );

        scheduleSummaryDto = new MaintenanceScheduleSummaryDto(
                1L,
                "Annual Fire Extinguisher Check",
                12,
                FrequencyUnit.MONTH
        );
    }

    private void setEntityId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    @DisplayName("Should create maintenance schedule successfully")
    void shouldCreateMaintenanceSchedule_WhenValidRequest() {
        // Given
        when(maintenanceScheduleRepository.existsByOrganizationIdAndName(anyLong(), anyString()))
                .thenReturn(false);
        when(maintenanceScheduleRepository.save(any(MaintenanceSchedule.class)))
                .thenReturn(testSchedule);
        when(maintenanceScheduleMapper.toDto(testSchedule))
                .thenReturn(scheduleDto);

        // When
        MaintenanceScheduleDto result = maintenanceScheduleService.createMaintenanceSchedule(createRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Annual Fire Extinguisher Check");
        verify(maintenanceScheduleRepository).existsByOrganizationIdAndName(1L, "Annual Fire Extinguisher Check");
        verify(maintenanceScheduleRepository).save(any(MaintenanceSchedule.class));
        verify(maintenanceScheduleMapper).toDto(testSchedule);
    }

    @Test
    @DisplayName("Should throw ConflictException when schedule name already exists")
    void shouldThrowConflictException_WhenScheduleNameAlreadyExists() {
        // Given
        when(maintenanceScheduleRepository.existsByOrganizationIdAndName(anyLong(), anyString()))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> maintenanceScheduleService.createMaintenanceSchedule(createRequest, testUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Maintenance schedule with name 'Annual Fire Extinguisher Check' already exists in this organization");

        verify(maintenanceScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find all schedules by organization")
    void shouldFindAllByOrganization_WhenValidOrganization() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MaintenanceSchedule> schedulePage = new PageImpl<>(List.of(testSchedule));
        
        when(maintenanceScheduleRepository.findAllByOrganizationId(1L, pageable))
                .thenReturn(schedulePage);
        when(maintenanceScheduleMapper.toSummaryDto(testSchedule))
                .thenReturn(scheduleSummaryDto);

        // When
        Page<MaintenanceScheduleSummaryDto> result = maintenanceScheduleService.findAllByOrganization(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Annual Fire Extinguisher Check");
        verify(maintenanceScheduleRepository).findAllByOrganizationId(1L, pageable);
    }

    @Test
    @DisplayName("Should find schedule by ID successfully")
    void shouldFindMaintenanceScheduleById_WhenValidIdAndUser() {
        // Given
        when(maintenanceScheduleRepository.findById(1L))
                .thenReturn(Optional.of(testSchedule));
        when(maintenanceScheduleMapper.toDto(testSchedule))
                .thenReturn(scheduleDto);

        // When
        MaintenanceScheduleDto result = maintenanceScheduleService.findMaintenanceScheduleById(1L, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Annual Fire Extinguisher Check");
        verify(maintenanceScheduleRepository).findById(1L);
        verify(maintenanceScheduleMapper).toDto(testSchedule);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when schedule not found")
    void shouldThrowResourceNotFoundException_WhenScheduleNotFound() {
        // Given
        when(maintenanceScheduleRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> maintenanceScheduleService.findMaintenanceScheduleById(999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Maintenance schedule not found with ID: 999");

        verify(maintenanceScheduleRepository).findById(999L);
        verify(maintenanceScheduleMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when accessing schedule from different organization")
    void shouldThrowAccessDeniedException_WhenAccessingScheduleFromDifferentOrganization() throws Exception {
        // Given
        Organization anotherOrganization = new Organization();
        setEntityId(anotherOrganization, 2L);
        anotherOrganization.setName("Another Organization");

        MaintenanceSchedule scheduleFromAnotherOrg = new MaintenanceSchedule();
        setEntityId(scheduleFromAnotherOrg, 1L);
        scheduleFromAnotherOrg.setOrganization(anotherOrganization);

        when(maintenanceScheduleRepository.findById(1L))
                .thenReturn(Optional.of(scheduleFromAnotherOrg));

        // When & Then
        assertThatThrownBy(() -> maintenanceScheduleService.findMaintenanceScheduleById(1L, testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to maintenance schedule");

        verify(maintenanceScheduleMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should search schedules by name")
    void shouldSearchMaintenanceSchedulesByName_WhenValidSearchTerm() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MaintenanceSchedule> schedulePage = new PageImpl<>(List.of(testSchedule));
        
        when(maintenanceScheduleRepository.findByOrganizationIdAndNameContainingIgnoreCase(1L, "fire", pageable))
                .thenReturn(schedulePage);
        when(maintenanceScheduleMapper.toSummaryDto(testSchedule))
                .thenReturn(scheduleSummaryDto);

        // When
        Page<MaintenanceScheduleSummaryDto> result = maintenanceScheduleService.searchMaintenanceSchedulesByName("fire", testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).contains("Fire");
        verify(maintenanceScheduleRepository).findByOrganizationIdAndNameContainingIgnoreCase(1L, "fire", pageable);
    }

    @Test
    @DisplayName("Should update schedule successfully")
    void shouldUpdateMaintenanceSchedule_WhenValidRequest() {
        // Given
        when(maintenanceScheduleRepository.findById(1L))
                .thenReturn(Optional.of(testSchedule));
        when(maintenanceScheduleRepository.existsByOrganizationIdAndName(1L, "Updated Schedule Name"))
                .thenReturn(false);
        when(maintenanceScheduleRepository.save(testSchedule))
                .thenReturn(testSchedule);
        when(maintenanceScheduleMapper.toDto(testSchedule))
                .thenReturn(scheduleDto);

        // When
        MaintenanceScheduleDto result = maintenanceScheduleService.updateMaintenanceSchedule(1L, updateRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        verify(maintenanceScheduleRepository).findById(1L);
        verify(maintenanceScheduleRepository).existsByOrganizationIdAndName(1L, "Updated Schedule Name");
        verify(maintenanceScheduleRepository).save(testSchedule);
    }

    @Test
    @DisplayName("Should throw ConflictException when updating to existing name")
    void shouldThrowConflictException_WhenUpdatingToExistingName() {
        // Given
        when(maintenanceScheduleRepository.findById(1L))
                .thenReturn(Optional.of(testSchedule));
        when(maintenanceScheduleRepository.existsByOrganizationIdAndName(1L, "Updated Schedule Name"))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> maintenanceScheduleService.updateMaintenanceSchedule(1L, updateRequest, testUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Maintenance schedule with name 'Updated Schedule Name' already exists in this organization");

        verify(maintenanceScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow updating schedule with same name")
    void shouldAllowUpdatingSchedule_WhenNameUnchanged() {
        // Given
        UpdateMaintenanceScheduleRequest sameNameRequest = new UpdateMaintenanceScheduleRequest(
                "Annual Fire Extinguisher Check", // Same name
                6,
                FrequencyUnit.MONTH
        );

        when(maintenanceScheduleRepository.findById(1L))
                .thenReturn(Optional.of(testSchedule));
        when(maintenanceScheduleRepository.save(testSchedule))
                .thenReturn(testSchedule);
        when(maintenanceScheduleMapper.toDto(testSchedule))
                .thenReturn(scheduleDto);

        // When
        MaintenanceScheduleDto result = maintenanceScheduleService.updateMaintenanceSchedule(1L, sameNameRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        verify(maintenanceScheduleRepository).findById(1L);
        verify(maintenanceScheduleRepository, never()).existsByOrganizationIdAndName(anyLong(), anyString());
        verify(maintenanceScheduleRepository).save(testSchedule);
    }

    @Test
    @DisplayName("Should delete schedule successfully")
    void shouldDeleteMaintenanceSchedule_WhenValidIdAndUser() {
        // Given
        when(maintenanceScheduleRepository.findById(1L))
                .thenReturn(Optional.of(testSchedule));

        // When
        maintenanceScheduleService.deleteMaintenanceSchedule(1L, testUser);

        // Then
        verify(maintenanceScheduleRepository).findById(1L);
        verify(maintenanceScheduleRepository).delete(testSchedule);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent schedule")
    void shouldThrowResourceNotFoundException_WhenDeletingNonExistentSchedule() {
        // Given
        when(maintenanceScheduleRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> maintenanceScheduleService.deleteMaintenanceSchedule(999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Maintenance schedule not found with ID: 999");

        verify(maintenanceScheduleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when deleting schedule from different organization")
    void shouldThrowAccessDeniedException_WhenDeletingScheduleFromDifferentOrganization() throws Exception {
        // Given
        Organization anotherOrganization = new Organization();
        setEntityId(anotherOrganization, 2L);

        MaintenanceSchedule scheduleFromAnotherOrg = new MaintenanceSchedule();
        setEntityId(scheduleFromAnotherOrg, 1L);
        scheduleFromAnotherOrg.setOrganization(anotherOrganization);

        when(maintenanceScheduleRepository.findById(1L))
                .thenReturn(Optional.of(scheduleFromAnotherOrg));

        // When & Then
        assertThatThrownBy(() -> maintenanceScheduleService.deleteMaintenanceSchedule(1L, testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to maintenance schedule");

        verify(maintenanceScheduleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MaintenanceSchedule> emptyPage = new PageImpl<>(List.of());
        
        when(maintenanceScheduleRepository.findByOrganizationIdAndNameContainingIgnoreCase(1L, "nonexistent", pageable))
                .thenReturn(emptyPage);

        // When
        Page<MaintenanceScheduleSummaryDto> result = maintenanceScheduleService.searchMaintenanceSchedulesByName("nonexistent", testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(maintenanceScheduleRepository).findByOrganizationIdAndNameContainingIgnoreCase(1L, "nonexistent", pageable);
    }
}