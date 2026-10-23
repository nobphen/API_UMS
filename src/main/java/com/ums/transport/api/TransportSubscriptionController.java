package com.ums.transport.api;

import com.ums.transport.application.TransportSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transport-subscriptions")
@RequiredArgsConstructor
public class TransportSubscriptionController {

    private final TransportSubscriptionService transportSubscriptionService;
}
