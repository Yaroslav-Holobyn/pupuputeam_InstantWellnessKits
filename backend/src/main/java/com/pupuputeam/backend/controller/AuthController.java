package com.pupuputeam.backend.controller;

import com.pupuputeam.backend.dto.request.AuthRequest;
import com.pupuputeam.backend.dto.response.AuthResponse;
import com.pupuputeam.backend.service.AuthService;
import com.pupuputeam.backend.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {

        return ResponseEntity.ok(new AuthResponse(authService.login(
                authRequest.getUserEmail(), authRequest.getPassword()
        )));
    }
}
