package com.safework.api.security;

import com.safework.api.domain.user.model.User;
import com.safework.api.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = repository.findByEmail(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return new UserPrincipal(user);
        } else {
            throw new UsernameNotFoundException("User not found!");
        }
    }
}