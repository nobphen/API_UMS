package com.ums.enrollment.attendance.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.enrollment.registration.domain.Enrollment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One student's attendance in one session. */
@Getter
@Setter
@Entity
@Table(name = "attendance_records")
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    /** The approved leave request that excuses the absence (request module). */
    @Column(name = "leave_request_id")
    private Long leaveRequestId;

    @Column(name = "note")
    private String note;

    public enum Status { PRESENT, ABSENT, LATE, EXCUSED }
}
