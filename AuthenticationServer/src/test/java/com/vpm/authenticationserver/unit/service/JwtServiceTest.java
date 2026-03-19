package com.vpm.authenticationserver.unit.service;


import com.vpm.authenticationserver.config.PropertiesConfig;
import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.service.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private PropertiesConfig prop;

    @Mock
    private JwtEncoder  jwtEncoder;

    @InjectMocks
    private JwtService jwtService;

    Jwt jwt = mock(Jwt.class);

    /*
     * Testing values
     */

    // Test token using https://www.jwt.io/ - payload specified, propper claims and RS256 algorithm
    private final String tokenTest = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3N1ZXIiOiJodHRwOi8vbG9jYWxob3N0OjgwODEiLCJzdWIiOiJ0ZXN0ZW1haWxAZ21haWwuY29tIiwiaWF0IjoxNzczOTk3MTU2LCJlbWFpbCI6InRlc3RlbWFpbEBnbWFpbC5jb20iLCJ1c2VySWQiOiIxIiwicm9sZSI6IlZPTFVOVEVFUiJ9.HOQZN518VLYTZe5c8bCYIPZ3ttxopr7Q9a4hXdsyFh2fO3yp6vxc8CZs1JoxiHooCxukhEQ4tESB-8BnvsuixIAIWGP9EtYCm82DX1KW1xxQM78JAG2PEpRr0pCu0QehfXkySH2MrN5_kZRLUjejy_AqMq2rKjKRAOml6nwVIExfsFANp9TVVkhsL4V8Mx_VwNPc9dZnw1EKwtjZV2ipjxjc9lzUTG2DB7I_jvMqwvFoLDTCSzzwsYxhENGkYN42rB2lbWpxojtOR6r4v5HSFrEw1jazLLOunJ9S9wmMsHhRyv9uLuADVV_oHW84Xv7CTDuz1VF9yO9OW2Cx4Tgplw";

    Users userTest1 = new Users(
            1,
            "testemail@gmail.com",
            "hashedpassword",
            "VOLUNTEER",
            null
    );

    Users userTest2 = new Users(
            2,
            "testemail2@gmail.com",
            "hashedpassword2",
            "ORGANIZATION",
            null
    );

    @BeforeEach
    public void setUp() {

        System.out.println("--- BEGIN OF TEST ---\n");

        // Mocks behaviour
        //TODO: move those mocks into classes as not all tests cover utility of all mocks
        when(prop.getJwtExpirationTime()).thenReturn(900L);
        when(prop.getJwtRefreshTokenExpirationTime()).thenReturn(24600L);
        when(jwt.getTokenValue()).thenReturn(tokenTest);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

    }

    @AfterEach
    public void closeUp() {
        System.out.println("\n--- END OF TEST ---\n");
    }

    @Nested
    @DisplayName("Testing access token")
    public class AccessTokenTest {

        private long accessTokenExpirationTimeTest = 900L;

        /*
         * Positive testing
         */

        @Test
        @DisplayName("Testing token generation")
        public void generateValidToken() {

            String token = jwtService.generateAccessToken(userTest1);

            assertNotNull(token);
            assertNotEquals("", token, "Token should not be empty");

        }

        @Test
        @DisplayName("Testing encoder call")
        public void testEncoderCall() {
            String token =  jwtService.generateAccessToken(userTest1);

            verify(jwtEncoder, times(1)).encode(any(JwtEncoderParameters.class));
        }

        @Test
        @DisplayName("Testing propper structure of token")
        public void shouldGenerateTokenWithValidStructure() {
            String token = jwtService.generateAccessToken(userTest1);

            String[] tokenParts = token.split("\\.");

            assertEquals(3, tokenParts.length, "Token should have 3-part structure (header, payload, sign)");
        }

        @Test
        @DisplayName("Testing propper claims generation")
        public void shouldGenerateTokenWithPropperClaims() {
            String token = jwtService.generateAccessToken(userTest1);

            assertEquals(tokenTest, token, "Token should be equal to test token");
        }

        /*
         * Negative testing
         */

        @Test
        @DisplayName("Testing null user")
        public void shouldThrowErrorOnNullUser() {

            reset(jwt, jwtEncoder, prop);

            assertThrows(NullPointerException.class, () -> jwtService.generateAccessToken(null));

        }

    }

}
