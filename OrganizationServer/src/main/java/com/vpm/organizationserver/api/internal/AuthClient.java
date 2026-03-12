package com.vpm.organizationserver.api.internal;

import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "auth-server", url = "${services.auth-service.url}")
public interface AuthClient {

    @PostMapping("/")
    AuthRegistrationResponse registerOrganizationInAuthServer(AuthRegistrationRequest request);
}
