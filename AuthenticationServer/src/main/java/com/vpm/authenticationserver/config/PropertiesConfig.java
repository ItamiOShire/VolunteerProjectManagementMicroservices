package com.vpm.authenticationserver.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class PropertiesConfig {

    @Value("${security.jwt.expiration}")
    private long jwtExpirationTime;

    @Value("${security.jwt.refresh-expiration}")
    private long jwtRefreshTokenExpirationTime;

    @Value("${security.jwt.private-key-path}")
    private Resource privateKey;

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private Resource publicKey;

}
