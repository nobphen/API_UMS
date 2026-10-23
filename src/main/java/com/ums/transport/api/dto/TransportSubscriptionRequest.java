package com.ums.transport.api.dto;

import jakarta.validation.constraints.NotNull;

/** Subscribes a student to a route for one semester; the fee invoice is made by the system. */
public record TransportSubscriptionRequest(
        @NotNull Long studentId,
        @NotNull Long routeId,
        Long stopId,   // where the student gets on; must be a stop of the route
        @NotNull Long semesterId) {
}
