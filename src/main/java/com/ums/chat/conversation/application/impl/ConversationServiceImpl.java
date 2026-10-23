package com.ums.chat.conversation.application.impl;

import com.ums.chat.conversation.application.ConversationService;
import com.ums.chat.conversation.infrastructure.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
}
