package com.vpm.authenticationserver.dto.request;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "email field cannot be null")
        @NotEmpty(message = "email field cannot be empty") String email,

        @NotEmpty(message = "password field cannot be null")
        @NotNull(message = "password field cannot be empty") String password) {
}
