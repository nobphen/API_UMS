package com.ums.engagement.event.api.dto;

import com.ums.engagement.event.domain.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record EventRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull Event.EventType eventType,
        @NotNull OffsetDateTime startAt,
        OffsetDateTime endAt,
        @Size(max = 200) String venue,
        Long roomId,
        Long organizerId,
        @Positive Integer capacity) {   // empty = no limit
}
