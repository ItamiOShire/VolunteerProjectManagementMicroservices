package com.vpm.authenticationserver.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String role,
        long id) {

}
