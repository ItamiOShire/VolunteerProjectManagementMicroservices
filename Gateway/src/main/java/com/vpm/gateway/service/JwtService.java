package com.vpm.gateway.service;

import com.vpm.gateway.properties.Properties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
@Slf4j
public class JwtService {

    private final SecretKey key;

    private final Map<Class<? extends Throwable>, Supplier<String>> ErrorsLogs =
            new HashMap<>(
                    Map.of(
                            ExpiredJwtException.class, () -> "Token expired: ",
                            SecurityException.class, () -> "Invalid signature",
                            MalformedJwtException.class, () -> "Invalid token: "
                    )
            );

    @Autowired
    public JwtService(
            Properties properties
    ) {
        this.key = Keys.hmacShaKeyFor(properties.getJwtSecretKey().getBytes());
    }

    private void validateTokenLog(
            Exception e
    ) {
        String errorCause = ErrorsLogs.getOrDefault(
                e.getClass(),
                () -> "Unexpected error: "
        ).get();

        log.error("{} {}", errorCause, e.getMessage());
    }

    public Claims getClaimsFromToken(
            String token
    ) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJwt(token)
                .getBody();
    }

    public Object getClaimFromToken(
            String token,
            String claimKey
    ) {
        return getClaimsFromToken(token)
                .getOrDefault(claimKey, "no-such-claim");
    }

    public long getExpirationTimeFromToken(
            String token
    ) {
        return getClaimsFromToken(token)
                .getExpiration()
                .getTime();
    }

    public boolean validateToken(
            String token
    ) {

        try {
            Jwts.parserBuilder().
                    setSigningKey(key).build()
                    .parseClaimsJwt(token);
            return true;
        } catch (Exception e) {
            validateTokenLog(e);
        }

        return false;
    }

}
