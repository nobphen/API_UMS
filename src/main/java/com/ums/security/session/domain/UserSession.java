package com.ums.security.session.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** One row per logged-in device. The refresh token is stored hashed and replaced on every refresh. */
@Getter
@Setter
@Entity
@Table(name = "user_sessions")
@EntityListeners(AuditingEntityListener.class)
public class UserSession extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "refresh_token_hash", nullable = false, unique = true)
    private String refreshTokenHash;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "device_model")
    private String deviceModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private Platform platform;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "app_name")
    private String appName;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_method", nullable = false)
    private LoginMethod loginMethod = LoginMethod.PASSWORD;

    @Column(name = "remember_me", nullable = false)
    private boolean rememberMe = true;

    /** Filled by JPA auditing (JpaConfig). */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by")
    private Long revokedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_reason")
    private RevokedReason revokedReason;

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }

    public enum Platform { WEB, ANDROID, IOS, DESKTOP }

    public enum LoginMethod { PASSWORD, QR, OTP }

    public enum RevokedReason {
        LOGOUT, TERMINATED_BY_USER, TERMINATE_OTHERS, PASSWORD_CHANGED, ADMIN, USER_LOCKED,
        INACTIVE, EXPIRED, MAX_DEVICES, TOKEN_REUSE, REPLACED
    }
}
