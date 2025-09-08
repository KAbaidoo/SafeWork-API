package com.safework.api.security;

import com.safework.api.domain.user.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * A custom UserDetails implementation that wraps the application's User entity.
 * This class provides user details to Spring Security.
 */
@Getter
public class PrincipalUser implements UserDetails {

    /**
     * -- GETTER --
     *  Returns the underlying User entity.
     */
    private final User user;

    public PrincipalUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converts the UserRole enum into a GrantedAuthority for Spring Security.
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // We use the email as the username for authentication.
        return user.getEmail();
    }

    // For this application, accounts are always active and enabled.
    // You could add logic here later to support disabled or locked accounts.

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}