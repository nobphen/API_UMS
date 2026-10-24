package com.ums.security.user.application.job;

import com.ums.security.role.domain.Role;
import com.ums.security.role.infrastructure.RoleRepository;
import com.ums.security.user.domain.User;
import com.ums.security.user.domain.UserRole;
import com.ums.security.user.infrastructure.UserRepository;
import com.ums.security.user.infrastructure.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first SUPER_ADMIN on a new server (SIT, UAT, PROD) from UMS_ADMIN_USERNAME / UMS_ADMIN_PASSWORD /
 * UMS_ADMIN_EMAIL, so nobody has to insert one by hand. It runs at every start but does nothing once any user holds
 * SUPER_ADMIN. The password must be changed at first login; remove the variables from the server afterwards.
 */
@Slf4j
@Component
public class FirstAdminSetup implements ApplicationRunner {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    private static final int MIN_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String email;

    public FirstAdminSetup(UserRepository userRepository, UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                           @Value("${ums.first-admin.username:}") String username,
                           @Value("${ums.first-admin.password:}") String password,
                           @Value("${ums.first-admin.email:}") String email) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username.trim();
        this.password = password;
        this.email = email.trim();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRoleRepository.countByRole_Code(SUPER_ADMIN) > 0) {
            return;   // the system already has an administrator
        }
        if (username.isEmpty() || password.isEmpty()) {
            log.warn("No SUPER_ADMIN exists yet. Set UMS_ADMIN_USERNAME and UMS_ADMIN_PASSWORD (and UMS_ADMIN_EMAIL) "
                    + "and restart to create the first administrator.");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("UMS_ADMIN_PASSWORD must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalStateException("UMS_ADMIN_USERNAME " + username + " already belongs to another user; choose another name");
        }
        Role role = roleRepository.findByCode(SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("Role SUPER_ADMIN is missing; check V3__seed_roles_and_admin.sql"));

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email.isEmpty() ? null : email.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setUserType(User.UserType.ADMIN);
        admin.setMustChangePassword(true);   // the password was in a server setting: replace it at first login
        userRepository.save(admin);

        UserRole userRole = new UserRole();
        userRole.setUser(admin);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        log.info("Created the first administrator '{}'. Log in, change the password, then remove UMS_ADMIN_* from the server.", username);
    }
}
