package com.vpm.authenticationserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {

    @NotBlank(message = "refresh token cannot be blank")
    private String refreshToken;

}
