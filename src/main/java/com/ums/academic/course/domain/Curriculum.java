package com.ums.academic.course.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One version of a program's study plan; the version is unique per program. */
@Getter
@Setter
@Entity
@Table(name = "curriculums")
public class Curriculum extends BaseEntity {

    /** The program (organization module). */
    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "version", nullable = false)
    private String version;

    /** First intake year this curriculum applies to. */
    @Column(name = "effective_year", nullable = false)
    private int effectiveYear;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
}
