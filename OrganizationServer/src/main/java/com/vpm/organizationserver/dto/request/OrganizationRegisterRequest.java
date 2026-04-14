package com.vpm.organizationserver.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationRegisterRequest {

    private String organizationName;

    private String krsNumber;

    private String street;

    private String apartmentNumber;

    private String town;

    private String zipCode;

    private String type;

    private String ownerFirstName;

    private String ownerLastName;

    private String phoneNumber;

    private String email;

    private String password;


}
