package com.safework.api.domain.department.mapper;

import com.safework.api.domain.department.dto.DepartmentDto;
import com.safework.api.domain.department.dto.DepartmentSummaryDto;
import com.safework.api.domain.department.model.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDto toDto(Department department) {
        return new DepartmentDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCode(),
                department.getOrganization() != null ? department.getOrganization().getId() : null,
                department.getManager() != null ? department.getManager().getId() : null,
                department.getManager() != null ? department.getManager().getName() : null,
                department.getEmployeeCount(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }

    public DepartmentSummaryDto toSummaryDto(Department department) {
        return new DepartmentSummaryDto(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getManager() != null ? department.getManager().getName() : null,
                department.getEmployeeCount()
        );
    }
}