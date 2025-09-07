package com.safework.api.security;

import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.repository.UserRepository;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
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
            String userEmail = jwtTokenProvider.getAuthUser(request);

            if (userEmail != null) {
                // Load the full User entity from the database
                Optional<User> userOptional = userRepository.findByEmail(userEmail);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserPrincipal principal = new UserPrincipal(user);
                    
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

