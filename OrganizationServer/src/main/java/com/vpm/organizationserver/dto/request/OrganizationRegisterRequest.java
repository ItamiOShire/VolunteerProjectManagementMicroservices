package com.vpm.organizationserver.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationRegisterRequest {

    @NotBlank(message = "organizationName field cannot be blank")
    @Size(max = 50, message = "organizationName cannot be longer than 50 characters")
    private String organizationName;

    @NotBlank(message = "KRS number field cannot be blank")
    @Size(min = 10, max = 10, message = "KRS number must be exactly 10 characters long")
    private String krsNumber;

    @NotBlank(message = "street field cannot be blank")
    @Size (max = 40, message = "street cannot be longer than 40 characters")
    private String street;

    @Size(max = 10, message = "apartment number cannot be longer than 10 characters")
    private String apartmentNumber;

    @NotBlank(message = "town field cannot be blank")
    @Size(max = 40, message = "town cannot be longer than 40 characters")
    private String town;

    @NotBlank(message = "zip code field cannot be blank")
    @Size(min = 6, max = 6, message = "zip code must be exactly 6 characters long")
    private String zipCode;

    @NotBlank(message = "owner first name cannot be blank")
    @Size(max = 25, message = "owner first name cannot be longer than 25 characters")
    private String ownerFirstName;

    @NotBlank(message = "owner last name cannot be blank")
    @Size(max = 50, message = "owner last name cannot be longer than 50 characters")
    private String ownerLastName;

    @NotBlank(message = "phone number field cannot be blank")
    @Size(min = 9, max = 12, message = "phone number must be between 9 (without prefix) and 12 (with prefix like +48) characters long")
    private String phoneNumber;

    @NotBlank(message = "contact email field cannot be blank")
    @Email(message = "contact email field must be a valid email address")
    private String contactEmail;

    @NotBlank(message = "email field cannot be blank")
    @Email
    private String email;

    @NotBlank(message = "password field cannot be blank")
    @Size(min = 8, max = 50, message = "password must be between 8 and 50 characters long")
    private String password;


}
