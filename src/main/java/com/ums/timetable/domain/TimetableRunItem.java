package com.ums.timetable.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/** A slot proposed by a timetable run for one class section. */
@Getter
@Setter
@Entity
@Table(name = "timetable_run_items")
public class TimetableRunItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id")
    private TimetableRun run;

    /** The class section (academic module). */
    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    /** The room (organization module). */
    @Column(name = "room_id")
    private Long roomId;

    /** 1 = Monday ... 7 = Sunday. */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
