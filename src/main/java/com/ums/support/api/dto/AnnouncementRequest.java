package com.ums.support.api.dto;

import com.ums.support.domain.Announcement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record AnnouncementRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 200) String titleKh,
        @NotBlank String content,
        String contentKh,
        Announcement.TargetType targetType,   // empty = ALL
        Long targetId,                        // required unless targetType is ALL
        OffsetDateTime expiresAt) {
}
