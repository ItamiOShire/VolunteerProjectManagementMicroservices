package com.vpm.authenticationserver.service;

import com.vpm.authenticationserver.dto.response.LoginResponse;
import com.vpm.authenticationserver.dto.request.LoginRequest;
import com.vpm.authenticationserver.entity.RefreshToken;
import com.vpm.authenticationserver.entity.Users;
import com.vpm.authenticationserver.exception.user.InvalidCredentialsException;
import com.vpm.authenticationserver.exception.user.UserNotFoundException;
import com.vpm.authenticationserver.repository.RefreshTokenRepository;
import com.vpm.authenticationserver.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private void validateLoginException(Exception e) {
        if (e instanceof UsernameNotFoundException) {
            log.error("User not found: {}", e.getMessage());
        } else if (e instanceof AuthenticationException){
            log.error("Authentication failed: {}", e.getMessage());
        } else {
            log.error("An unexpected error occurred during login: {}", e.getMessage());
        }
    }


    public LoginResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {

        log.info("Attempting to authenticate user with email: {}", userLogin.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            Users user = (Users) authentication.getPrincipal();
            assert user != null;

            String jwt = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            RefreshToken refreshTokenEntity = new RefreshToken(
                    refreshToken,
                    Date.from(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationTime())),
                    user
            );

            refreshTokenRepository.save(refreshTokenEntity);

            return new LoginResponse(
                    jwt,
                    refreshToken,
                    user.getRole(),
                    user.getId()
            );

        } catch (AuthenticationException e) {

            validateLoginException(e);
            throw new InvalidCredentialsException();

        }

    }

}
