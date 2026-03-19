package com.vpm.authenticationserver.service;

import com.nimbusds.jose.*;
import com.vpm.authenticationserver.config.PropertiesConfig;
import com.vpm.authenticationserver.entity.Users;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
public class JwtService {

    private final long accessTokenExpirationTime; // in seconds

    @Getter
    private final long refreshTokenExpirationTime; // in seconds

    private final JwtEncoder  jwtEncoder;

    @Autowired
    public JwtService(
            PropertiesConfig propertiesConfig,
            JwtEncoder jwtEncoder) {
        this.accessTokenExpirationTime = propertiesConfig.getJwtExpirationTime();
        this.refreshTokenExpirationTime = propertiesConfig.getJwtRefreshTokenExpirationTime();
        this.jwtEncoder = jwtEncoder;
    }

    public String generateAccessToken(Users user) {

        return generateTokenByExpirationTime(
                user,
                accessTokenExpirationTime
        );

    }

    public String generateRefreshToken(Users user) {

        return generateTokenByExpirationTime(
                user,
                refreshTokenExpirationTime
        );

    }

    private Map<String, Object> generateDefaultClaims(
            Users user) {

        return Map.of(
                "email", user.getEmail(),
                "userId", user.getId(),
                "role", user.getRole()
        );

    }

    private String generateTokenByExpirationTime(Users user, long expirationTime) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8081")
                .issuedAt(now)
                .subject(user.getEmail())
                .expiresAt(now.plusSeconds(expirationTime))
                .claims(
                        map -> map.putAll(
                                generateDefaultClaims(
                                        user
                                )
                        )
                )
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

    }



}
