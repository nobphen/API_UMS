package com.ums.enrollment.grading.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Kind of assessment: quiz, assignment, midterm, final, ... */
@Getter
@Setter
@Auditable
@Entity
@Table(name = "assessment_types")
public class AssessmentType extends SoftDeletableEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_kh")
    private String nameKh;
}
