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
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceLogService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceLogMapper maintenanceLogMapper;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public MaintenanceLogDto createMaintenanceLog(CreateMaintenanceLogRequest request, User currentUser) {
        // Validate asset exists and belongs to user's organization
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + request.assetId()));

        if (!asset.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied to asset");
        }

        // Validate technician if provided
        User technician = null;
        if (request.technicianId() != null) {
            technician = userRepository.findById(request.technicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + request.technicianId()));

            if (!technician.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new AccessDeniedException("Access denied to technician");
            }
        }

        MaintenanceLog newLog = new MaintenanceLog();
        newLog.setAsset(asset);
        newLog.setTechnician(technician);
        newLog.setServiceDate(request.serviceDate());
        newLog.setNotes(request.notes());
        newLog.setCost(request.cost());

        MaintenanceLog savedLog = maintenanceLogRepository.save(newLog);
        return maintenanceLogMapper.toDto(savedLog);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceLogSummaryDto> findAllByOrganization(Long organizationId, Pageable pageable) {
        Page<MaintenanceLog> logs = maintenanceLogRepository.findByOrganizationIdOrderByServiceDateDesc(organizationId, pageable);
        return logs.map(maintenanceLogMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceLogSummaryDto> findByAsset(Long assetId, User currentUser, Pageable pageable) {
        // Validate asset access
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        if (!asset.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied to asset");
        }

        Page<MaintenanceLog> logs = maintenanceLogRepository.findByAssetIdOrderByServiceDateDesc(assetId, pageable);
        return logs.map(maintenanceLogMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceLogSummaryDto> findByTechnician(Long technicianId, User currentUser, Pageable pageable) {
        // Validate technician access
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + technicianId));

        if (!technician.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied to technician");
        }

        Page<MaintenanceLog> logs = maintenanceLogRepository.findByTechnicianIdOrderByServiceDateDesc(technicianId, pageable);
        return logs.map(maintenanceLogMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceLogSummaryDto> findByDateRange(LocalDate startDate, LocalDate endDate, User currentUser, Pageable pageable) {
        Page<MaintenanceLog> logs = maintenanceLogRepository.findByOrganizationIdAndServiceDateBetween(
                currentUser.getOrganization().getId(), startDate, endDate, pageable);
        return logs.map(maintenanceLogMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public MaintenanceLogDto findMaintenanceLogById(Long id, User currentUser) {
        MaintenanceLog log = getMaintenanceLogForUser(id, currentUser);
        return maintenanceLogMapper.toDto(log);
    }

    @Transactional(readOnly = true)
    public Optional<MaintenanceLogDto> findMostRecentByAsset(Long assetId, User currentUser) {
        // Validate asset access
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with ID: " + assetId));

        if (!asset.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied to asset");
        }

        return maintenanceLogRepository.findMostRecentByAssetId(assetId)
                .stream()
                .findFirst()
                .map(maintenanceLogMapper::toDto);
    }

    public MaintenanceLogDto updateMaintenanceLog(Long id, UpdateMaintenanceLogRequest request, User currentUser) {
        MaintenanceLog logToUpdate = getMaintenanceLogForUser(id, currentUser);

        // Validate technician if provided
        User technician = null;
        if (request.technicianId() != null) {
            technician = userRepository.findById(request.technicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + request.technicianId()));

            if (!technician.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                throw new AccessDeniedException("Access denied to technician");
            }
        }

        logToUpdate.setTechnician(technician);
        logToUpdate.setServiceDate(request.serviceDate());
        logToUpdate.setNotes(request.notes());
        logToUpdate.setCost(request.cost());

        MaintenanceLog savedLog = maintenanceLogRepository.save(logToUpdate);
        return maintenanceLogMapper.toDto(savedLog);
    }

    public void deleteMaintenanceLog(Long id, User currentUser) {
        MaintenanceLog logToDelete = getMaintenanceLogForUser(id, currentUser);
        maintenanceLogRepository.delete(logToDelete);
    }

    /**
     * Helper method to retrieve a maintenance log and validate user access.
     * Ensures the user can only access logs for assets within their organization.
     */
    private MaintenanceLog getMaintenanceLogForUser(Long id, User currentUser) {
        MaintenanceLog log = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance log not found with ID: " + id));

        // Ensure user can only access logs for assets from their organization
        if (!log.getAsset().getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new AccessDeniedException("Access denied to maintenance log");
        }

        return log;
    }
}