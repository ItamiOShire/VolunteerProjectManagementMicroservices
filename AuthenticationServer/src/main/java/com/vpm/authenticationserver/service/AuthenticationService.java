package com.vpm.authenticationserver.service;

import com.vpm.authenticationserver.dto.response.LoginResponse;
import com.vpm.authenticationserver.dto.request.LoginRequest;
import com.vpm.authenticationserver.entity.RefreshToken;
import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.exception.user.InvalidCredentialsException;
import com.vpm.authenticationserver.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    private final Map<Class<? extends Throwable>, Supplier<String>> errors =
            new HashMap<>(
                    Map.of(
                            UsernameNotFoundException.class, () -> "User not found: ",
                            AuthenticationException.class, () -> "Authentication failed: "
                    )
            );

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private void validateLoginException(Exception e) {
        Supplier<String> errorMessage = errors
                .getOrDefault(
                        e.getClass(),
                        () -> "Unexpected error during login: "
                );
        log.error("{} {}", errorMessage.get(), e.getMessage());
    }


    public LoginResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {

        log.info("Attempting to authenticate user with email: {}", loginRequest.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            Users user = (Users) authentication.getPrincipal();
            assert user != null;

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            RefreshToken refreshTokenEntity = new RefreshToken(
                    refreshToken,
                    Instant.now().plusSeconds(jwtService.getRefreshTokenExpirationTime()),
                    user
            );

            log.info("Refresh token generated: {}", refreshTokenEntity);

            refreshTokenRepository.save(refreshTokenEntity);

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    user
            );

        } catch (Exception e) {

            validateLoginException(e);
            throw new InvalidCredentialsException();

        }

    }

}
