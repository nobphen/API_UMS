package com.ums.chat.notification.domain;

import com.ums.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends AuditableEntity {

    /** The login account (security module) the notification is for. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message")
    private String message;

    /** Where the app goes when the notification is opened. */
    @Column(name = "link")
    private String link;

    @Column(name = "is_read", nullable = false)
    private boolean read;
}
