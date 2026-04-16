package com.vpm.volunteerserver.dto.request;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class VolunteerRegisterRequest {

    @NotBlank(message = "first name field cannot be blank")
    @Size(max= 25, message = "first name cannot be longer than 25 characters")
    private String firstName;

    @NotBlank(message = "last name field cannot be blank")
    @Size(max = 50, message = "last name cannot be longer than 50")
    private String lastName;

    @NotBlank(message = "email field cannot be blank")
    @Email(message = "email field must be a valid email address")
    private String email;

    @NotBlank(message = "password field cannot be blank")
    @Size(min = 8, max = 50, message = "password must be between 8 and 50 characters long")
    private String password;

    @NotBlank(message = "date of birth field cannot be blank")
    @Past(message = "date of birth must be a date in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "phone number field cannot be blank")
    @Size(min = 9, max = 12, message = "phone number must be between 9 (without prefix) and 12 (with prefix like +48) characters long")
    private String phoneNumber;

}
