package com.vpm.gateway.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Properties {

    @Value("${security.jwt.secret}")
    private String jwtSecretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpirationTime; // in ms

    @Value("${security.jwt.refresh-expiration}")
    private long jwtRefreshExpirationTime;

}
