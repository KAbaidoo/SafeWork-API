package com.safework.api.domain.supplier.mapper;

import com.safework.api.domain.supplier.dto.SupplierDto;
import com.safework.api.domain.supplier.dto.SupplierSummaryDto;
import com.safework.api.domain.supplier.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierDto toDto(Supplier supplier) {
        return new SupplierDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhoneNumber(),
                supplier.getAddress(),
                supplier.getOrganization() != null ? supplier.getOrganization().getId() : null,
                supplier.getVersion(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }

    public SupplierSummaryDto toSummaryDto(Supplier supplier) {
        return new SupplierSummaryDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhoneNumber()
        );
    }
}