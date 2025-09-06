package com.safework.api.config;

import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.repository.UserRepository;
import com.safework.api.security.CustomUserPrincipal;
import com.safework.api.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;


@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get token from Authorization header
        String jws = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (jws != null) {
            // Verify token and get user email
            String userEmail = jwtService.getAuthUser(request);

            if (userEmail != null) {
                // Load the full User entity from the database
                Optional<User> userOptional = userRepository.findByEmail(userEmail);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    CustomUserPrincipal principal = new CustomUserPrincipal(user);
                    
                    // Create authentication with proper authorities
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            user,  // Use the User entity directly as the principal
                            null,
                            Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                            )
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

