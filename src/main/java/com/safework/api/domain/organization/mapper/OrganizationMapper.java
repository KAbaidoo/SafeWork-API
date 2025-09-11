package com.safework.api.domain.organization.mapper;

import com.safework.api.domain.organization.dto.OrganizationDto;
import com.safework.api.domain.organization.dto.OrganizationSummaryDto;
import com.safework.api.domain.organization.model.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public OrganizationDto toDto(Organization organization) {
        return new OrganizationDto(
                organization.getId(),
                organization.getName(),
                organization.getAddress(),
                organization.getPhone(),
                organization.getWebsite(),
                organization.getIndustry(),
                organization.getSize() != null ? organization.getSize().name() : null,
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }

    public OrganizationSummaryDto toSummaryDto(Organization organization) {
        return new OrganizationSummaryDto(
                organization.getId(),
                organization.getName(),
                organization.getIndustry(),
                organization.getSize() != null ? organization.getSize().name() : null
        );
    }
}