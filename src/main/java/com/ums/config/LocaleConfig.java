package com.ums.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Which language the API answers in: Khmer or English. Messages come from i18n/messages*.properties.
 * Order: the app's Accept-Language header, then the language saved for the logged-in user (the token's
 * "lang" claim), then ums.default-language (en).
 */
@Configuration
public class LocaleConfig {

    public static final List<Locale> SUPPORTED = List.of(Locale.forLanguageTag("km"), Locale.ENGLISH);

    @Bean
    public LocaleResolver localeResolver(@Value("${ums.default-language:en}") String defaultLanguage) {
        Locale fallback = Locale.forLanguageTag(defaultLanguage);
        return new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String header = request.getHeader("Accept-Language");
                if (header != null && !header.isBlank()) {
                    try {
                        Locale match = Locale.lookup(Locale.LanguageRange.parse(header), SUPPORTED);
                        if (match != null) return match;
                    } catch (IllegalArgumentException ignored) {
                        // malformed header: fall through to the user's or the default language
                    }
                }
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth instanceof JwtAuthenticationToken jwt && jwt.getToken().getClaimAsString("lang") != null) {
                    return Locale.forLanguageTag(jwt.getToken().getClaimAsString("lang"));
                }
                return fallback;
            }

            @Override
            public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
                throw new UnsupportedOperationException("Send Accept-Language, or change the user's language");
            }
        };
    }
}
