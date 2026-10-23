package com.ums.chat.conversation.infrastructure;

import com.ums.chat.conversation.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
