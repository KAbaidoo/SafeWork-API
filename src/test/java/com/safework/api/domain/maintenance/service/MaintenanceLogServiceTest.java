package com.safework.api.domain.maintenance.service;

import com.safework.api.domain.asset.model.Asset;
import com.safework.api.domain.asset.repository.AssetRepository;
import com.safework.api.domain.maintenance.dto.CreateMaintenanceLogRequest;
import com.safework.api.domain.maintenance.dto.MaintenanceLogDto;
import com.safework.api.domain.maintenance.dto.MaintenanceLogSummaryDto;
import com.safework.api.domain.maintenance.dto.UpdateMaintenanceLogRequest;
import com.safework.api.domain.maintenance.mapper.MaintenanceLogMapper;
import com.safework.api.domain.maintenance.model.MaintenanceLog;
import com.safework.api.domain.maintenance.repository.MaintenanceLogRepository;
import com.safework.api.domain.organization.model.Organization;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.model.UserRole;
import com.safework.api.domain.user.repository.UserRepository;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaintenanceLogService Tests")
class MaintenanceLogServiceTest {

    @Mock
    private MaintenanceLogRepository maintenanceLogRepository;

    @Mock
    private MaintenanceLogMapper maintenanceLogMapper;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MaintenanceLogService maintenanceLogService;

    private Organization testOrganization;
    private User testUser;
    private User testTechnician;
    private Asset testAsset;
    private MaintenanceLog testLog;
    private CreateMaintenanceLogRequest createRequest;
    private UpdateMaintenanceLogRequest updateRequest;
    private MaintenanceLogDto logDto;
    private MaintenanceLogSummaryDto logSummaryDto;

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

        // Setup technician
        testTechnician = new User();
        setEntityId(testTechnician, 2L);
        testTechnician.setEmail("tech@example.com");
        testTechnician.setName("Jane Smith");
        testTechnician.setRole(UserRole.INSPECTOR);
        testTechnician.setOrganization(testOrganization);

        // Setup asset
        testAsset = new Asset();
        setEntityId(testAsset, 1L);
        testAsset.setAssetTag("TEST-001");
        testAsset.setName("Test Asset");
        testAsset.setOrganization(testOrganization);

        // Setup maintenance log
        testLog = new MaintenanceLog();
        setEntityId(testLog, 1L);
        testLog.setAsset(testAsset);
        testLog.setTechnician(testTechnician);
        testLog.setServiceDate(LocalDate.now().minusDays(1));
        testLog.setNotes("Routine maintenance");
        testLog.setCost(new BigDecimal("150.00"));

        // Setup DTOs
        createRequest = new CreateMaintenanceLogRequest(
                1L,
                2L,
                LocalDate.now().minusDays(1),
                "Routine maintenance",
                new BigDecimal("150.00")
        );

        updateRequest = new UpdateMaintenanceLogRequest(
                2L,
                LocalDate.now(),
                "Updated maintenance notes",
                new BigDecimal("200.00")
        );

        logDto = new MaintenanceLogDto(
                1L,
                1L,
                "Test Asset",
                "TEST-001",
                2L,
                "Jane Smith",
                LocalDate.now().minusDays(1),
                "Routine maintenance",
                new BigDecimal("150.00")
        );

        logSummaryDto = new MaintenanceLogSummaryDto(
                1L,
                1L,
                "Test Asset",
                "TEST-001",
                LocalDate.now().minusDays(1),
                new BigDecimal("150.00")
        );
    }

    private void setEntityId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    @DisplayName("Should create maintenance log successfully")
    void shouldCreateMaintenanceLog_WhenValidRequest() {
        // Given
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testTechnician));
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(testLog);
        when(maintenanceLogMapper.toDto(testLog)).thenReturn(logDto);

        // When
        MaintenanceLogDto result = maintenanceLogService.createMaintenanceLog(createRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.assetName()).isEqualTo("Test Asset");
        verify(assetRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(maintenanceLogRepository).save(any(MaintenanceLog.class));
        verify(maintenanceLogMapper).toDto(testLog);
    }

    @Test
    @DisplayName("Should create maintenance log without technician")
    void shouldCreateMaintenanceLog_WithoutTechnician() {
        // Given
        CreateMaintenanceLogRequest requestWithoutTechnician = new CreateMaintenanceLogRequest(
                1L, null, LocalDate.now(), "Self-service", new BigDecimal("50.00"));

        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(testLog);
        when(maintenanceLogMapper.toDto(testLog)).thenReturn(logDto);

        // When
        MaintenanceLogDto result = maintenanceLogService.createMaintenanceLog(requestWithoutTechnician, testUser);

        // Then
        assertThat(result).isNotNull();
        verify(assetRepository).findById(1L);
        verify(userRepository, never()).findById(any());
        verify(maintenanceLogRepository).save(any(MaintenanceLog.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when asset not found")
    void shouldThrowResourceNotFoundException_WhenAssetNotFound() {
        // Given
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        CreateMaintenanceLogRequest invalidRequest = new CreateMaintenanceLogRequest(
                999L, 2L, LocalDate.now(), "Notes", BigDecimal.TEN);

        // When & Then
        assertThatThrownBy(() -> maintenanceLogService.createMaintenanceLog(invalidRequest, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Asset not found with ID: 999");

        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when asset belongs to different organization")
    void shouldThrowAccessDeniedException_WhenAssetBelongsToDifferentOrganization() throws Exception {
        // Given
        Organization anotherOrganization = new Organization();
        setEntityId(anotherOrganization, 2L);

        Asset assetFromAnotherOrg = new Asset();
        setEntityId(assetFromAnotherOrg, 1L);
        assetFromAnotherOrg.setOrganization(anotherOrganization);

        when(assetRepository.findById(1L)).thenReturn(Optional.of(assetFromAnotherOrg));

        // When & Then
        assertThatThrownBy(() -> maintenanceLogService.createMaintenanceLog(createRequest, testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to asset");

        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when technician not found")
    void shouldThrowResourceNotFoundException_WhenTechnicianNotFound() {
        // Given
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        CreateMaintenanceLogRequest invalidRequest = new CreateMaintenanceLogRequest(
                1L, 999L, LocalDate.now(), "Notes", BigDecimal.TEN);

        // When & Then
        assertThatThrownBy(() -> maintenanceLogService.createMaintenanceLog(invalidRequest, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Technician not found with ID: 999");

        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find all logs by organization")
    void shouldFindAllByOrganization_WhenValidOrganization() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MaintenanceLog> logPage = new PageImpl<>(List.of(testLog));

        when(maintenanceLogRepository.findByOrganizationIdOrderByServiceDateDesc(1L, pageable))
                .thenReturn(logPage);
        when(maintenanceLogMapper.toSummaryDto(testLog)).thenReturn(logSummaryDto);

        // When
        Page<MaintenanceLogSummaryDto> result = maintenanceLogService.findAllByOrganization(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(maintenanceLogRepository).findByOrganizationIdOrderByServiceDateDesc(1L, pageable);
    }

    @Test
    @DisplayName("Should find logs by asset")
    void shouldFindByAsset_WhenValidAssetAndUser() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MaintenanceLog> logPage = new PageImpl<>(List.of(testLog));

        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(maintenanceLogRepository.findByAssetIdOrderByServiceDateDesc(1L, pageable))
                .thenReturn(logPage);
        when(maintenanceLogMapper.toSummaryDto(testLog)).thenReturn(logSummaryDto);

        // When
        Page<MaintenanceLogSummaryDto> result = maintenanceLogService.findByAsset(1L, testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(assetRepository).findById(1L);
        verify(maintenanceLogRepository).findByAssetIdOrderByServiceDateDesc(1L, pageable);
    }

    @Test
    @DisplayName("Should find logs by technician")
    void shouldFindByTechnician_WhenValidTechnicianAndUser() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MaintenanceLog> logPage = new PageImpl<>(List.of(testLog));

        when(userRepository.findById(2L)).thenReturn(Optional.of(testTechnician));
        when(maintenanceLogRepository.findByTechnicianIdOrderByServiceDateDesc(2L, pageable))
                .thenReturn(logPage);
        when(maintenanceLogMapper.toSummaryDto(testLog)).thenReturn(logSummaryDto);

        // When
        Page<MaintenanceLogSummaryDto> result = maintenanceLogService.findByTechnician(2L, testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findById(2L);
        verify(maintenanceLogRepository).findByTechnicianIdOrderByServiceDateDesc(2L, pageable);
    }

    @Test
    @DisplayName("Should find logs by date range")
    void shouldFindByDateRange_WhenValidDatesAndUser() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<MaintenanceLog> logPage = new PageImpl<>(List.of(testLog));

        when(maintenanceLogRepository.findByOrganizationIdAndServiceDateBetween(1L, startDate, endDate, pageable))
                .thenReturn(logPage);
        when(maintenanceLogMapper.toSummaryDto(testLog)).thenReturn(logSummaryDto);

        // When
        Page<MaintenanceLogSummaryDto> result = maintenanceLogService.findByDateRange(startDate, endDate, testUser, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(maintenanceLogRepository).findByOrganizationIdAndServiceDateBetween(1L, startDate, endDate, pageable);
    }

    @Test
    @DisplayName("Should find maintenance log by ID")
    void shouldFindMaintenanceLogById_WhenValidIdAndUser() {
        // Given
        when(maintenanceLogRepository.findById(1L)).thenReturn(Optional.of(testLog));
        when(maintenanceLogMapper.toDto(testLog)).thenReturn(logDto);

        // When
        MaintenanceLogDto result = maintenanceLogService.findMaintenanceLogById(1L, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.assetName()).isEqualTo("Test Asset");
        verify(maintenanceLogRepository).findById(1L);
        verify(maintenanceLogMapper).toDto(testLog);
    }

    @Test
    @DisplayName("Should find most recent log by asset")
    void shouldFindMostRecentByAsset_WhenValidAssetAndUser() {
        // Given
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(maintenanceLogRepository.findMostRecentByAssetId(1L)).thenReturn(List.of(testLog));
        when(maintenanceLogMapper.toDto(testLog)).thenReturn(logDto);

        // When
        Optional<MaintenanceLogDto> result = maintenanceLogService.findMostRecentByAsset(1L, testUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().assetName()).isEqualTo("Test Asset");
        verify(assetRepository).findById(1L);
        verify(maintenanceLogRepository).findMostRecentByAssetId(1L);
    }

    @Test
    @DisplayName("Should return empty when no recent log found")
    void shouldReturnEmpty_WhenNoRecentLogFound() {
        // Given
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(maintenanceLogRepository.findMostRecentByAssetId(1L)).thenReturn(List.of());

        // When
        Optional<MaintenanceLogDto> result = maintenanceLogService.findMostRecentByAsset(1L, testUser);

        // Then
        assertThat(result).isEmpty();
        verify(assetRepository).findById(1L);
        verify(maintenanceLogRepository).findMostRecentByAssetId(1L);
    }

    @Test
    @DisplayName("Should update maintenance log successfully")
    void shouldUpdateMaintenanceLog_WhenValidRequest() {
        // Given
        when(maintenanceLogRepository.findById(1L)).thenReturn(Optional.of(testLog));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testTechnician));
        when(maintenanceLogRepository.save(testLog)).thenReturn(testLog);
        when(maintenanceLogMapper.toDto(testLog)).thenReturn(logDto);

        // When
        MaintenanceLogDto result = maintenanceLogService.updateMaintenanceLog(1L, updateRequest, testUser);

        // Then
        assertThat(result).isNotNull();
        verify(maintenanceLogRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(maintenanceLogRepository).save(testLog);
    }

    @Test
    @DisplayName("Should delete maintenance log successfully")
    void shouldDeleteMaintenanceLog_WhenValidIdAndUser() {
        // Given
        when(maintenanceLogRepository.findById(1L)).thenReturn(Optional.of(testLog));

        // When
        maintenanceLogService.deleteMaintenanceLog(1L, testUser);

        // Then
        verify(maintenanceLogRepository).findById(1L);
        verify(maintenanceLogRepository).delete(testLog);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent log")
    void shouldThrowResourceNotFoundException_WhenUpdatingNonExistentLog() {
        // Given
        when(maintenanceLogRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> maintenanceLogService.updateMaintenanceLog(999L, updateRequest, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Maintenance log not found with ID: 999");

        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when accessing log from different organization")
    void shouldThrowAccessDeniedException_WhenAccessingLogFromDifferentOrganization() throws Exception {
        // Given
        Organization anotherOrganization = new Organization();
        setEntityId(anotherOrganization, 2L);

        Asset assetFromAnotherOrg = new Asset();
        setEntityId(assetFromAnotherOrg, 1L);
        assetFromAnotherOrg.setOrganization(anotherOrganization);

        MaintenanceLog logFromAnotherOrg = new MaintenanceLog();
        setEntityId(logFromAnotherOrg, 1L);
        logFromAnotherOrg.setAsset(assetFromAnotherOrg);

        when(maintenanceLogRepository.findById(1L)).thenReturn(Optional.of(logFromAnotherOrg));

        // When & Then
        assertThatThrownBy(() -> maintenanceLogService.findMaintenanceLogById(1L, testUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to maintenance log");

        verify(maintenanceLogMapper, never()).toDto(any());
    }
}