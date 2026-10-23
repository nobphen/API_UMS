package com.ums.transport.application.impl;

import com.ums.transport.application.TransportSubscriptionService;
import com.ums.transport.infrastructure.TransportSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TransportSubscriptionServiceImpl implements TransportSubscriptionService {

    private final TransportSubscriptionRepository transportSubscriptionRepository;
}
