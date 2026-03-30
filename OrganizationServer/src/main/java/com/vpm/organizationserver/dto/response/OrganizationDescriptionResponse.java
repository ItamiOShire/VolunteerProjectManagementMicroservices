package com.vpm.organizationserver.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationDescriptionResponse {

    private String organizationName;

    private String description;

    private String imagePath;

}
