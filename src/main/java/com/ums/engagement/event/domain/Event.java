package com.ums.engagement.event.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_kh")
    private String titleKh;

    @Column(name = "description")
    private String description;

    @Column(name = "description_kh")
    private String descriptionKh;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    /** Free-text place, for events held outside the campus rooms. */
    @Column(name = "venue")
    private String venue;

    /** A room on campus (facility module). */
    @Column(name = "room_id")
    private Long roomId;

    /** The login account (security module) organising the event. */
    @Column(name = "organizer_id")
    private Long organizerId;

    /** Maximum registrations; null = no limit. */
    @Column(name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;

    public enum EventType { SEMINAR, WORKSHOP, CEREMONY, ALUMNI, SPORTS }

    public enum Status { DRAFT, PUBLISHED, CANCELLED, COMPLETED }
}
