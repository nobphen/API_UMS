package com.ums.enrollment.registration.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** A student registered in one class section. */
@Getter
@Setter
@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    @Column(name = "enrolled_at", nullable = false)
    private OffsetDateTime enrolledAt = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ENROLLED;

    /** The student is taking the course again after failing or dropping it. */
    @Column(name = "is_retake", nullable = false)
    private boolean retake = false;

    public enum Status { ENROLLED, DROPPED, COMPLETED, WITHDRAWN }
}
