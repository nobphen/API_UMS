package com.ums.chat.conversation.api.dto;

import com.ums.chat.conversation.domain.Message;
import java.time.OffsetDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        Message.MessageType messageType,
        String content,
        Long replyToId,
        OffsetDateTime editedAt,
        OffsetDateTime createdAt) {
}
