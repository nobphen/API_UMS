package com.ums.chat.conversation.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * One member of a chat (table conversation_participants). Has its own id (from BaseEntity);
 * the pair conversation + user is UNIQUE in the database, so a user is in a conversation only once.
 */
@Getter
@Setter
@Entity
@Table(name = "conversation_participants")
public class ConversationParticipant extends BaseEntity {

    /** The chat (conversations.id). */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /** The member (users.id). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** MEMBER or ADMIN of this chat. */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.MEMBER;

    /** The last message this participant has seen; later messages count as unread. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private Message lastReadMessage;

    /** True when the member turned off notifications for this chat. */
    @Column(name = "is_muted", nullable = false)
    private boolean muted;

    /** When the member joined the chat. */
    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt = OffsetDateTime.now();

    /** Set when the participant leaves or is removed; null while still a member. */
    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    /** The member's role in the chat. */
    public enum Role { MEMBER, ADMIN }
}
