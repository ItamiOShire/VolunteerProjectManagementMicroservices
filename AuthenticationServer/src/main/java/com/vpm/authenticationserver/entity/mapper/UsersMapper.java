package com.vpm.authenticationserver.entity.mapper;

import com.vpm.authenticationserver.entity.Users;
import com.vpm.common.dto.request.AuthRegistrationRequest;

public class UsersMapper {

    public static Users map(AuthRegistrationRequest request) {

        return Users.builder()
                .email(request.getEmail())
                .role(request.getRole())
                .password(request.getPassword())
                .build();
    }

}
