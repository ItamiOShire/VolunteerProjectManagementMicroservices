package com.vpm.authenticationserver.controller;


import com.vpm.authenticationserver.dto.request.LoginRequest;
import com.vpm.authenticationserver.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Validated @RequestBody LoginRequest loginRequest
            ) {
        return ResponseEntity
                .ok(authenticationService.login(loginRequest));
    }

}
