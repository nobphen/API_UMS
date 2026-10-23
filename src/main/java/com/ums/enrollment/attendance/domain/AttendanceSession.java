package com.ums.enrollment.attendance.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/** One class meeting where attendance is taken. */
@Getter
@Setter
@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession extends BaseEntity {

    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "topic")
    private String topic;

    /** The staff member who took attendance. */
    @Column(name = "taken_by")
    private Long takenBy;
}
