package com.vpm.organizationserver.entity.mapper;

import com.vpm.organizationserver.dto.request.CreateDescriptionRequest;
import com.vpm.organizationserver.entity.Organization;
import com.vpm.organizationserver.entity.OrganizationDescription;

public class OrganizationDescriptionMapper {

    public static OrganizationDescription fromCreateDescriptionRequest(
            CreateDescriptionRequest request,
            Organization organization
    ) {

        return OrganizationDescription.builder()
                .description(request.getDescription())
                .imagePath(request.getImagePath())
                .organization(organization)
                .build();
    }

}
