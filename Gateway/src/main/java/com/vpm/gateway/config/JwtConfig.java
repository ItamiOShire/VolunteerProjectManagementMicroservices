package com.vpm.gateway.config;

import com.vpm.gateway.properties.PropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    private final PropertiesConfig properties;

    @Autowired
    public JwtConfig(PropertiesConfig properties) {
        this.properties = properties;
    }

    @Bean
    public RSAPublicKey  loadPublicKey()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String keyContent = new String(
                properties.getPublicKey().getInputStream().readAllBytes()
        );

        keyContent = keyContent
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey  publicKey) {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

}
