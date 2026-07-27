package com.safework.api.domain.department.dto;

public record DepartmentSummaryDto(
        Long id,
        String name,
        String code,
        String managerName,
        Integer employeeCount
) {}