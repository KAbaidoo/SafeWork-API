package com.safework.api.domain.auth.service;

import com.safework.api.domain.auth.dto.LoginRequest;
import com.safework.api.domain.auth.dto.LoginResponse;
import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            // Generate JWT token
            String token = jwtService.getToken(loginRequest.email());

            // Get user details
            User user = userRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            // Build and return the response
            return new LoginResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getOrganization() != null ? user.getOrganization().getId() : null
            );

        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password", e);
        }
    }
}