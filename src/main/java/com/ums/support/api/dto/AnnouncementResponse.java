package com.ums.support.api.dto;

import com.ums.support.domain.Announcement;
import java.time.OffsetDateTime;

public record AnnouncementResponse(
        Long id,
        String title,
        String content,
        Announcement.TargetType targetType,
        Long targetId,
        Long publishedBy,
        OffsetDateTime publishedAt,
        OffsetDateTime expiresAt) {
}
