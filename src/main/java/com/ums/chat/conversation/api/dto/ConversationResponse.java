package com.ums.chat.conversation.api.dto;

import com.ums.chat.conversation.domain.Conversation;
import java.time.OffsetDateTime;

public record ConversationResponse(
        Long id,
        Conversation.Type type,
        String title,
        Long classSectionId,
        String referenceType,
        Long referenceId,
        OffsetDateTime lastMessageAt,
        Long createdBy,
        OffsetDateTime createdAt) {
}
