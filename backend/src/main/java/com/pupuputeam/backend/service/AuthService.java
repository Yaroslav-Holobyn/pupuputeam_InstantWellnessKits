package com.pupuputeam.backend.service;

import com.pupuputeam.backend.exception.AuthException;
import com.pupuputeam.backend.model.User;
import com.pupuputeam.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public String login(String userEmail, String password) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AuthException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Wrong password");
        }

        return jwtService.generateToken(userEmail);
    }
}
