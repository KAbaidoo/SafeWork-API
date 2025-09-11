package com.safework.api.domain.department.dto;

import java.time.LocalDateTime;

public record DepartmentDto(
        Long id,
        String name,
        String description,
        String code,
        Long organizationId,
        Long managerId,
        String managerName,
        Integer employeeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}