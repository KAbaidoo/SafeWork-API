package com.safework.api.domain.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Code must be 2-10 uppercase letters or numbers")
        String code,

        Long managerId
) {}