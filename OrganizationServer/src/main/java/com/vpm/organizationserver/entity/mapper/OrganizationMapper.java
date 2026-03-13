package com.vpm.organizationserver.entity.mapper;

import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.organizationserver.dto.request.OrganizationRegisterRequest;
import com.vpm.organizationserver.entity.Organization;

public class OrganizationMapper {

    public static Organization mapFromRegistrationRequestAndAuthResponse(
            OrganizationRegisterRequest request, AuthRegistrationResponse response
    ) {
        return Organization.builder()
                .organizationName(request.getOrganizationName())
                .krsNumber(request.getKrsNumber())
                .street(request.getStreet())
                .apartmentNumber(request.getApartmentNumber().isEmpty() ? "-" : request.getApartmentNumber())
                .town(request.getTown())
                .zipCode(request.getZipCode())
                .ownerFirstName(request.getOwnerFirstName())
                .ownerLastName(request.getOwnerLastName())
                .phoneNumber(request.getPhoneNumber())
                .userId(response.getUserId())
                .build();
    }

}
