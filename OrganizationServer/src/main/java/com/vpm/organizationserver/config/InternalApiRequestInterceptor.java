package com.vpm.organizationserver.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class InternalApiRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-INTERNAL-REQUEST", "true");
        template.header("X-SERVICE-NAME", "organization-service");
    }
}

