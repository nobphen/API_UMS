package com.ums.academic.course.domain;

import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course extends SoftDeletableEntity {

    /** The department that owns the course (organization module). */
    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_kh")
    private String titleKh;

    @Column(name = "credits", nullable = false)
    private int credits;

    /** Weekly theory hours. */
    @Column(name = "theory_hours", nullable = false)
    private int theoryHours = 0;

    /** Weekly lab hours. */
    @Column(name = "lab_hours", nullable = false)
    private int labHours = 0;

    @Column(name = "description")
    private String description;
}
