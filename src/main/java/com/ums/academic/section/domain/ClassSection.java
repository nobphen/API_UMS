package com.ums.academic.section.domain;

import com.ums.academic.calendar.domain.Semester;
import com.ums.academic.course.domain.Course;
import com.ums.common.entity.BaseEntity;
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

/** One class of a course in a semester; the section code is unique per course and semester. */
@Getter
@Setter
@Entity
@Table(name = "class_sections")
public class ClassSection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id")
    private Semester semester;

    @Column(name = "section_code", nullable = false)
    private String sectionCode;

    /** Maximum number of enrolled students. */
    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.OPEN;

    public enum Status { OPEN, CLOSED, CANCELLED, COMPLETED }
}
