package com.safework.api.domain.maintenance.controller;

import com.safework.api.domain.maintenance.dto.CreateMaintenanceLogRequest;
import com.safework.api.domain.maintenance.dto.MaintenanceLogDto;
import com.safework.api.domain.maintenance.dto.MaintenanceLogSummaryDto;
import com.safework.api.domain.maintenance.dto.UpdateMaintenanceLogRequest;
import com.safework.api.domain.maintenance.service.MaintenanceLogService;
import com.safework.api.domain.user.model.User;
import com.safework.api.security.PrincipalUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/maintenance-logs")
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    /**
     * Creates a new maintenance log. Requires ADMIN or SUPERVISOR role.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<MaintenanceLogDto> createMaintenanceLog(
            @Valid @RequestBody CreateMaintenanceLogRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        MaintenanceLogDto newLog = maintenanceLogService.createMaintenanceLog(request, currentUser);
        return new ResponseEntity<>(newLog, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all maintenance logs for the current user's organization.
     * All authenticated users can view logs.
     */
    @GetMapping
    public ResponseEntity<Page<MaintenanceLogSummaryDto>> getMaintenanceLogsByOrganization(
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<MaintenanceLogSummaryDto> logs = maintenanceLogService.findAllByOrganization(
                currentUser.getOrganization().getId(), pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Retrieves a single maintenance log by its unique ID.
     * All authenticated users can view log details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceLogDto> getMaintenanceLogById(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        MaintenanceLogDto log = maintenanceLogService.findMaintenanceLogById(id, currentUser);
        return ResponseEntity.ok(log);
    }

    /**
     * Retrieves maintenance logs for a specific asset.
     * All authenticated users can view asset logs.
     */
    @GetMapping("/asset/{assetId}")
    public ResponseEntity<Page<MaintenanceLogSummaryDto>> getMaintenanceLogsByAsset(
            @PathVariable Long assetId,
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<MaintenanceLogSummaryDto> logs = maintenanceLogService.findByAsset(assetId, currentUser, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Retrieves the most recent maintenance log for a specific asset.
     */
    @GetMapping("/asset/{assetId}/recent")
    public ResponseEntity<MaintenanceLogDto> getMostRecentMaintenanceLogByAsset(
            @PathVariable Long assetId,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        Optional<MaintenanceLogDto> log = maintenanceLogService.findMostRecentByAsset(assetId, currentUser);
        return log.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves maintenance logs performed by a specific technician.
     * All authenticated users can view technician logs.
     */
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<Page<MaintenanceLogSummaryDto>> getMaintenanceLogsByTechnician(
            @PathVariable Long technicianId,
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<MaintenanceLogSummaryDto> logs = maintenanceLogService.findByTechnician(technicianId, currentUser, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Retrieves maintenance logs within a specific date range.
     * All authenticated users can search by date range.
     */
    @GetMapping("/search/date-range")
    public ResponseEntity<Page<MaintenanceLogSummaryDto>> getMaintenanceLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal PrincipalUser principalUser,
            Pageable pageable) {
        User currentUser = principalUser.getUser();
        Page<MaintenanceLogSummaryDto> logs = maintenanceLogService.findByDateRange(startDate, endDate, currentUser, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Updates an existing maintenance log. Requires ADMIN or SUPERVISOR role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<MaintenanceLogDto> updateMaintenanceLog(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaintenanceLogRequest request,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        MaintenanceLogDto updatedLog = maintenanceLogService.updateMaintenanceLog(id, request, currentUser);
        return ResponseEntity.ok(updatedLog);
    }

    /**
     * Deletes a maintenance log by its unique ID. Requires ADMIN role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteMaintenanceLog(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalUser principalUser) {
        User currentUser = principalUser.getUser();
        maintenanceLogService.deleteMaintenanceLog(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}