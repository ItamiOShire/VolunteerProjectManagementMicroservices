package com.vpm.organizationserver.api.internal;

import com.vpm.common.dto.request.AuthRegistrationRequest;
import com.vpm.common.dto.response.AuthRegistrationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", url = "${services.auth-service.url}")
public interface AuthClient {

    @PostMapping("/api/internal/register")
    AuthRegistrationResponse registerOrganizationInAuthServer(@RequestBody AuthRegistrationRequest request);

}
