package com.ums.exam.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

/** When and where a class section sits an exam. */
@Getter
@Setter
@Entity
@Table(name = "exam_schedules")
public class ExamSchedule extends BaseEntity {

    @Column(name = "class_section_id", nullable = false)
    private Long classSectionId;

    /** The assessment the exam is scored under (enrollment module). */
    @Column(name = "assessment_id")
    private Long assessmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "room_id")
    private Long roomId;

    public enum ExamType { MIDTERM, FINAL, RE_EXAM }
}
