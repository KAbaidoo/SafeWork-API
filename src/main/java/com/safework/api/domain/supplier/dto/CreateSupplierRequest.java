package com.safework.api.domain.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
        @NotBlank(message = "Supplier name is required")
        @Size(min = 2, max = 100, message = "Supplier name must be between 2 and 100 characters")
        String name,

        @Size(max = 100, message = "Contact person name must not exceed 100 characters")
        String contactPerson,

        @Email(message = "Email should be a valid email address")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "Phone number must be a valid format")
        String phoneNumber,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address
) {}