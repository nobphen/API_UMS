package com.ums.chat.conversation.domain;

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
@Table(name = "conversations")
public class Conversation extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @Column(name = "title")
    private String title;

    /** Set for a CLASS conversation (academic module). */
    @Column(name = "class_section_id")
    private Long classSectionId;

    /** The record a LEAVE_REQUEST or STUDENT_REQUEST conversation is about, e.g. LEAVE_REQUEST and its id. */
    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;

    public enum Type { DIRECT, GROUP, CLASS, LEAVE_REQUEST, STUDENT_REQUEST }
    /** The user who started the conversation. */
    @Column(name = "created_by")
    private Long createdBy;

    /** Filled by the database when the row is created. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
