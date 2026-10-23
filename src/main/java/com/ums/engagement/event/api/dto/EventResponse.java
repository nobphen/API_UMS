package com.ums.engagement.event.api.dto;

import com.ums.engagement.event.domain.Event;
import java.time.OffsetDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        Event.EventType eventType,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String venue,
        Long roomId,
        Long organizerId,
        Integer capacity,
        Event.Status status) {
}
