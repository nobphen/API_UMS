package com.ums.config;

import com.ums.security.filter.SessionActivityFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;                       // from PasswordEncoderConfig
    private final UserDetailsService userDetailsService;                 // CustomUserDetailsService
    private final JwtAuthenticationConverter jwtAuthenticationConverter; // from JwtConfig
    private final SessionActivityFilter sessionActivityFilter;

    /// Lets @RequirePermission(...) fill its codes into its @PreAuthorize expression
    @Bean
    static AnnotationTemplateExpressionDefaults configureAnnotationTemplates() {
        return new AnnotationTemplateExpressionDefaults();
    }

    /// Login: checks username + password against the database (used by AuthService.login)
    @Bean
    DaoAuthenticationProvider configDaoAuthenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder);
        return auth;
    }

    /// Refresh: checks a refresh token's signature and expiry (used by AuthService.refresh)
    @Bean
    JwtAuthenticationProvider configJwtAuthenticationProvider(JwtDecoder jwtDecoder) {
        return new JwtAuthenticationProvider(jwtDecoder);
    }

    @Bean
    SecurityFilterChain configureApiSecurity(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {

        /// Endpoint Security Config
        http.authorizeHttpRequests(endpoint -> endpoint
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout", "/api/v1/auth/qr/*/approve",
                        "/api/v1/auth/password/change").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/ws/**", "/actuator/health").permitAll()
                // Per-endpoint rules go on controller methods with permission codes, e.g.
                // @RequirePermission(PeoplePermissions.STUDENT_VIEW)
                .anyRequest()
                .authenticated()
        );

        /// Security Mechanism ( JWT ) — every request's Bearer token is checked with the public key
        http.oauth2ResourceServer(jwt -> jwt.jwt(jwtConfigurer -> jwtConfigurer
                .decoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter)));

        /// Track session activity after the token is verified
        http.addFilterAfter(sessionActivityFilter, BearerTokenAuthenticationFilter.class);

        /// CORS ( allowed origins come from CorsConfig )
        http.cors(Customizer.withDefaults());

        /// Disable CSRF Token
        http.csrf(AbstractHttpConfigurer::disable);

        /// Make Stateless Session
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
