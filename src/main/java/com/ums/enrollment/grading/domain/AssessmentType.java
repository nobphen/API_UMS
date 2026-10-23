package com.ums.enrollment.grading.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Kind of assessment: quiz, assignment, midterm, final, ... */
@Getter
@Setter
@Entity
@Table(name = "assessment_types")
public class AssessmentType extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;
}
