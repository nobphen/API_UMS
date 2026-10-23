package com.ums.academic.course.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A course in a curriculum, placed at the year and semester it is normally taken. */
@Getter
@Setter
@Entity
@Table(name = "curriculum_courses")
public class CurriculumCourse {

    @EmbeddedId
    private Id id;

    @Column(name = "year_level", nullable = false)
    private int yearLevel;

    /** Semester within the year level (1, 2, ...). */
    @Column(name = "semester_no", nullable = false)
    private int semesterNo;

    /** false = elective. */
    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class Id implements Serializable {

        @Column(name = "curriculum_id")
        private Long curriculumId;

        @Column(name = "course_id")
        private Long courseId;
    }
}
