package com.vpm.organizationserver.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationProfileResponse {

    private String name;

    private String krsNumber;

    // Combination of street, apartment number, town and zip code
    private String address;

    // Combination of owner's first and last name
    private String owner;

    // Combination of organization contact email and phone number
    private String contact;

}
