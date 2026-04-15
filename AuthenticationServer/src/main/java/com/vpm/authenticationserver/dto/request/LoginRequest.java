package com.vpm.authenticationserver.dto.request;


import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "email field cannot be blank")
        @Email
        String email,

        @NotBlank(message = "password field cannot be null")
        @Size(min = 8, max = 50, message = "password must be between 8 and 50 characters long")
        String password
) {
}
