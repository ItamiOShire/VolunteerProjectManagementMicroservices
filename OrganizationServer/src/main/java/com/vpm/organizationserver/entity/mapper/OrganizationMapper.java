package com.vpm.organizationserver.entity.mapper;

import com.vpm.common.dto.response.AuthRegistrationResponse;
import com.vpm.organizationserver.dto.request.OrganizationRegisterRequest;
import com.vpm.organizationserver.dto.response.OrganizationDescriptionResponse;
import com.vpm.organizationserver.dto.response.OrganizationProfileResponse;
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

    public static OrganizationProfileResponse toOrganizationProfileResponse(
            Organization organization
    ) {

        StringBuilder address = new StringBuilder();

        address.append(organization.getStreet())
                .append(" / ")
                .append(organization.getApartmentNumber() == null ? "-" : organization.getApartmentNumber())
                .append(", ")
                .append(organization.getZipCode())
                .append(" ")
                .append(organization.getTown());

        StringBuilder owner = new StringBuilder();
        owner.append(organization.getOwnerFirstName())
                .append(" ")
                .append(organization.getOwnerLastName());

        StringBuilder contact = new StringBuilder();
        contact.append(organization.getPhoneNumber())
                .append(" / ")
                .append(organization.getContactEmail());

        return OrganizationProfileResponse.builder()
                .name(organization.getOrganizationName())
                .krsNumber(organization.getKrsNumber())
                .address(address.toString())
                .owner(owner.toString())
                .contact(contact.toString())
                .build();
    }

    public static OrganizationDescriptionResponse toOrganizationDescriptionResponse(Organization organization) {

        return OrganizationDescriptionResponse.builder()
                .organizationName(organization.getOrganizationName())
                .description(organization.getOrganizationDescription().getDescription())
                .imagePath(organization.getOrganizationDescription().getImagePath())
                .build();

    }

}
