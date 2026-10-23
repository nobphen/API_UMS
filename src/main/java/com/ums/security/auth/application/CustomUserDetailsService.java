package com.ums.security.auth.application;

import com.ums.security.auth.AuthUser;
import com.ums.security.user.domain.User;
import com.ums.security.user.infrastructure.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Loads a user by username or email for login, with authorities from their active roles. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionCacheService permissionCacheService;

    @Override
    public AuthUser loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));

        // Fresh from the database at login; also refreshes the Redis cache used at /auth/refresh
        List<SimpleGrantedAuthority> authorities = permissionCacheService.reload(user.getId()).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        return new AuthUser(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.getUserType(), user.getStatus(), authorities);
    }
}
