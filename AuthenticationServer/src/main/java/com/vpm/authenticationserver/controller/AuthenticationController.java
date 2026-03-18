package com.vpm.authenticationserver.controller;


import com.vpm.authenticationserver.dto.request.LoginRequest;
import com.vpm.authenticationserver.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest
            ) {
        return ResponseEntity
                .ok(authenticationService.login(loginRequest));
    }

}
