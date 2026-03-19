package com.vpm.authenticationserver.unit.service;

import com.vpm.authenticationserver.dto.request.LoginRequest;
import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.exception.user.InvalidCredentialsException;
import com.vpm.authenticationserver.repository.RefreshTokenRepository;
import com.vpm.authenticationserver.service.AuthenticationService;
import com.vpm.authenticationserver.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.vpm.authenticationserver.dto.response.LoginResponse;
import com.vpm.authenticationserver.entity.RefreshToken;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    AuthenticationService authenticationService;

    // Logging capture
    private ListAppender<ILoggingEvent> listAppender;

    // Testing objects

    private LoginRequest loginRequest;
    private Users mockedUser;

    @BeforeEach
    public void setup(){

        System.out.println("--- BEGIN OF TEST ---\n");

        loginRequest = new LoginRequest(
                "testemail@gmail.com",
                "testpassword"
        );
        mockedUser = new Users(
                1,
                "testemail@gmail.com",
                "hashedpassword",
                "VOLUNTEER",
                null
        );

        // Setup logging capture
        Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    public void closeup() {
        // Clean up logging appender
        Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationService.class);
        logger.detachAppender(listAppender);
        listAppender.stop();

        System.out.println("\n--- END OF TEST ---\n");
    }

    private enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    private List<ILoggingEvent> getLogsByLevel(
            List<ILoggingEvent> logs,
            Level level
    ){
        return logs.stream()
                .filter(log -> log.getLevel().toString().equals(level.toString()))
                .collect(Collectors.toList());
    }

    /*
     * Positive testing
     */

    @Test
    @DisplayName("Testing successful user login")
    public void successfulLogin() {

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockedUser);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mockAuthentication);

        String mockAccessToken = "mock-access-token";
        String mockRefreshToken = "mock-refresh-token";
        
        when(jwtService.generateAccessToken(mockedUser)).thenReturn(mockAccessToken);
        when(jwtService.generateRefreshToken(mockedUser)).thenReturn(mockRefreshToken);
        when(jwtService.getRefreshTokenExpirationTime()).thenReturn(86400L); // 24 hours

        LoginResponse response = authenticationService.login(loginRequest);

        assertNotNull(response, "LoginResponse should not be null");
        assertEquals(mockAccessToken, response.accessToken(), "Access token should match");
        assertEquals(mockRefreshToken, response.refreshToken(), "Refresh token should match");
        assertEquals("VOLUNTEER", response.role(), "Role should match");
        assertEquals(1L, response.id(), "User ID should match");


        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateAccessToken(mockedUser);
        verify(jwtService, times(1)).generateRefreshToken(mockedUser);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    /*
     * Negative testing
     */

    @Test
    @DisplayName("Should throw one error on failing finding user in database")
    public void shouldThrowErrorOnNotFindingUser() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException(loginRequest.email()));

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(loginRequest), "Should throw InvalidCredentialsException under authentication failure");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Verify error logging
        var logs = getLogsByLevel(listAppender.list, Level.ERROR);
        assertEquals(1, logs.size(), "Should have one error log");
        assertTrue(logs.get(0).getFormattedMessage().startsWith("User not found: "));
    }

    @Test
    @DisplayName("Should throw one error on authentication failing")
    public void shouldThrowErrorOnAuthenticationFailure() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(loginRequest), "Should throw InvalidCredentialsException under authentication failure");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Verify error logging
        var logs = getLogsByLevel(listAppender.list, Level.ERROR);
        assertEquals(1, logs.size(), "Should have one error log");
        assertTrue(logs.get(0).getFormattedMessage().startsWith("Authentication failed: "));
    }

}
