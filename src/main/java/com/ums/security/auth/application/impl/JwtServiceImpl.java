package com.ums.security.auth.application.impl;

import com.ums.security.auth.application.JwtService;
import com.ums.security.user.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String ISSUER = "ums";

    private final JwtEncoder jwtEncoder;

    @Getter
    private final Duration accessTokenTtl;

    @Getter
    private final Duration refreshTokenTtl;

    public JwtServiceImpl(JwtEncoder jwtEncoder,
                          @Value("${ums.jwt.access-token-ttl}") Duration accessTokenTtl,
                          @Value("${ums.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Override
    public String generateAccessToken(User user, Long sessionId, Collection<String> authorities) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(user.getId()))
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl))
                .claim("typ", "access")
                .claim("sid", sessionId)
                .claim("username", user.getUsername())
                .claim("user_type", user.getUserType().name())
                .claim("authorities", List.copyOf(authorities))
                .claim("pwd_change", user.isMustChangePassword())   // SessionActivityFilter allows only a password change
                .claim("lang", user.getPreferredLanguage())          // the app shows this language after login
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public String generateRefreshToken(Long userId, Long sessionId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenTtl))
                .claim("typ", "refresh")
                .claim("sid", sessionId)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
