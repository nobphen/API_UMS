package com.ums.chat.conversation.infrastructure;

import com.ums.chat.conversation.domain.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipant.Id> {
}
