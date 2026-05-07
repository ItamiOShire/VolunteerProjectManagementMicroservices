package com.vpm.authenticationserver.dto.response;

import com.vpm.authenticationserver.entity.Users;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String role,
        Long id) {

    public LoginResponse(
            String accessToken,
            String refreshToken,
            Users user
    ) {
        this(
                accessToken,
                refreshToken,
                user.getRole(),
                user.getId()
        );
    }

}
