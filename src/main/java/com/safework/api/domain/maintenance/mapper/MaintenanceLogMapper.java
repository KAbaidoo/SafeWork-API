package com.safework.api.domain.maintenance.mapper;

import com.safework.api.domain.maintenance.dto.MaintenanceLogDto;
import com.safework.api.domain.maintenance.dto.MaintenanceLogSummaryDto;
import com.safework.api.domain.maintenance.model.MaintenanceLog;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceLogMapper {

    public MaintenanceLogDto toDto(MaintenanceLog log) {
        return new MaintenanceLogDto(
                log.getId(),
                log.getAsset() != null ? log.getAsset().getId() : null,
                log.getAsset() != null ? log.getAsset().getName() : null,
                log.getAsset() != null ? log.getAsset().getAssetTag() : null,
                log.getTechnician() != null ? log.getTechnician().getId() : null,
                log.getTechnician() != null ? log.getTechnician().getName() : null,
                log.getServiceDate(),
                log.getNotes(),
                log.getCost()
        );
    }

    public MaintenanceLogSummaryDto toSummaryDto(MaintenanceLog log) {
        return new MaintenanceLogSummaryDto(
                log.getId(),
                log.getAsset() != null ? log.getAsset().getId() : null,
                log.getAsset() != null ? log.getAsset().getName() : null,
                log.getAsset() != null ? log.getAsset().getAssetTag() : null,
                log.getServiceDate(),
                log.getCost()
        );
    }
}