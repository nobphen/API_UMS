package com.ums.chat.conversation.infrastructure;

import com.ums.chat.conversation.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
