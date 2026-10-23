package com.ums.chat.conversation.infrastructure;

import com.ums.chat.conversation.domain.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
}
