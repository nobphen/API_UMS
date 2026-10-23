package com.ums.chat.conversation.api.dto;

import com.ums.chat.conversation.domain.Message;
import jakarta.validation.constraints.NotNull;

public record MessageRequest(
        @NotNull Long conversationId,
        Message.MessageType messageType,   // empty = TEXT
        String content,
        Long replyToId) {
}
