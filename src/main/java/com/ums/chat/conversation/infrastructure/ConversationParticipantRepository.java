package com.ums.chat.conversation.infrastructure;

import com.ums.chat.conversation.domain.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for chat members (table conversation_participants). The key is a normal id (Long). */
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
}
