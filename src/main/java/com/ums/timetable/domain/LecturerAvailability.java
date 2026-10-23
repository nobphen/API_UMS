package com.ums.timetable.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/** A weekly time window in which a lecturer can (or cannot) teach during a semester. */
@Getter
@Setter
@Entity
@Table(name = "lecturer_availability")
public class LecturerAvailability extends BaseEntity {

    /** The lecturer (people module). */
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    /** The semester (academic module). */
    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    /** 1 = Monday ... 7 = Sunday. */
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** false = the lecturer is NOT available in this window. */
    @Column(name = "is_available", nullable = false)
    private boolean available = true;
}
