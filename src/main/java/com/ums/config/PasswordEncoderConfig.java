package com.ums.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Single PasswordEncoder for the whole app: inject it wherever a password is hashed or checked. */
@Configuration
public class PasswordEncoderConfig {

    /** BCrypt encoder: passwords are saved only as BCrypt hashes. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
