package com.vpm.volunteerserver.api.internal;

import com.vpm.volunteerserver.dto.request.AuthRequest;
import com.vpm.volunteerserver.dto.response.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "auth-service", url = "${services.auth-service.url")
public interface AuthClient {

    @PostMapping("/user")
    AuthResponse registerUserInAuthService(AuthRequest authRequest);

}
