package com.ums.support.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType = TargetType.ALL;

    /** The role, faculty, program or class section the announcement is for; null when targetType is ALL. */
    @Column(name = "target_id")
    private Long targetId;

    /** The login account (security module) that published it. */
    @Column(name = "published_by")
    private Long publishedBy;

    /** Null while still a draft. */
    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    public enum TargetType { ALL, ROLE, FACULTY, PROGRAM, CLASS_SECTION }
}
