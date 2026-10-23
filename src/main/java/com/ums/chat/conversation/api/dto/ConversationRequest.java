package com.ums.chat.conversation.api.dto;

import com.ums.chat.conversation.domain.Conversation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConversationRequest(
        @NotNull Conversation.Type type,
        @Size(max = 200) String title,
        Long classSectionId,   // for a CLASS conversation
        @Size(max = 30) String referenceType,
        Long referenceId) {
}
