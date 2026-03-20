package com.vpm.common.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegistrationRequest {

    private String email;
    private String password;
    private String role;

}
