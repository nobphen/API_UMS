package com.ums.security.auth;

import com.ums.security.user.domain.User;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** The logged-in user as Spring Security sees it: account state plus role (ROLE_*) and permission authorities. */
@Getter
@AllArgsConstructor
public class AuthUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final User.UserType userType;
    private final User.Status status;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonLocked() {
        return status != User.Status.LOCKED;
    }

    @Override
    public boolean isEnabled() {
        return status != User.Status.DISABLED && status != User.Status.PENDING;   // PENDING: email not verified yet
    }
}
