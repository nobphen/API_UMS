package com.ums.timetable.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** One execution of the timetable generator for a semester; its proposed slots are the run items. */
@Getter
@Setter
@Entity
@Table(name = "timetable_runs")
public class TimetableRun extends BaseEntity {

    /** The semester (academic module). */
    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    /** The user who started the run (security module). */
    @Column(name = "started_by")
    private Long startedBy;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.RUNNING;

    /** Soft-rule violations left in the result. */
    @Column(name = "conflict_count")
    private Integer conflictCount;

    /** Quality score of the result. */
    @Column(name = "score")
    private BigDecimal score;

    /** APPROVED: the result was accepted and copied into the class schedules. */
    public enum Status { RUNNING, DONE, FAILED, APPROVED }
}
