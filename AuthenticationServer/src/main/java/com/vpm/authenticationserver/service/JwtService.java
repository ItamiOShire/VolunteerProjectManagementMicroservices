package com.vpm.authenticationserver.service;

import com.vpm.authenticationserver.config.PropertiesConfig;
import com.vpm.authenticationserver.entity.Users;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtService {

    private final String secretKey;

    private final long expirationTime; // in milliseconds

    @Getter
    private final long refreshTokenExpirationTime; // in milliseconds

    private final SecretKey key;

    private final PropertiesConfig propertiesConfig;

    @Autowired
    public JwtService(PropertiesConfig propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
        this.secretKey = propertiesConfig.getJwtSecretKey();
        this.expirationTime = propertiesConfig.getJwtExpirationTime();
        this.refreshTokenExpirationTime = propertiesConfig.getJwtRefreshTokenExpirationTime();
        this.key = Keys.hmacShaKeyFor(this.secretKey.getBytes());
    }

    public String generateToken(Users user) {

        Date now = new Date();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(generateDefaultClaims(
                        user.getEmail(),
                        user.getId()))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(key)
                .compact();

    }

    public String generateRefreshToken(Users user) {

        Date now = new Date();

        return Jwts.builder()
                .setSubject(user.getEmail())
                .addClaims(generateDefaultClaims(
                        user.getEmail(),
                        user.getId()))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpirationTime))
                .signWith(key)
                .compact();

    }

    private Map<String, Object> generateDefaultClaims(String email, long id) {

        return Map.of(
                "email", email,
                "id", id
        );

    }

}
