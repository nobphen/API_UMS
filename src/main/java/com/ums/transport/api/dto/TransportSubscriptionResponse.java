package com.ums.transport.api.dto;

import com.ums.transport.domain.TransportSubscription;

public record TransportSubscriptionResponse(
        Long id,
        Long studentId,
        Long routeId,
        Long stopId,
        Long semesterId,
        Long invoiceId,
        TransportSubscription.Status status) {
}
