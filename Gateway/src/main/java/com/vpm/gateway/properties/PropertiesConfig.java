package com.vpm.gateway.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PropertiesConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private Resource publicKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpirationTime; // in seconds

    @Value("${security.jwt.refresh-expiration}")
    private long jwtRefreshExpirationTime; // in seconds

}
