package com.ums.chat.conversation.application.impl;

import com.ums.chat.conversation.application.MessageService;
import com.ums.chat.conversation.infrastructure.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
}
