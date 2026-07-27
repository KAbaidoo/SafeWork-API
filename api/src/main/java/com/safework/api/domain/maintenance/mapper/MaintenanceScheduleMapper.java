package com.safework.api.domain.maintenance.mapper;

import com.safework.api.domain.maintenance.dto.MaintenanceScheduleDto;
import com.safework.api.domain.maintenance.dto.MaintenanceScheduleSummaryDto;
import com.safework.api.domain.maintenance.model.MaintenanceSchedule;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceScheduleMapper {

    public MaintenanceScheduleDto toDto(MaintenanceSchedule schedule) {
        return new MaintenanceScheduleDto(
                schedule.getId(),
                schedule.getName(),
                schedule.getFrequencyInterval(),
                schedule.getFrequencyUnit(),
                schedule.getOrganization() != null ? schedule.getOrganization().getId() : null
        );
    }

    public MaintenanceScheduleSummaryDto toSummaryDto(MaintenanceSchedule schedule) {
        return new MaintenanceScheduleSummaryDto(
                schedule.getId(),
                schedule.getName(),
                schedule.getFrequencyInterval(),
                schedule.getFrequencyUnit()
        );
    }
}