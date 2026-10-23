package com.ums.admission.application.domain;

import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Someone applying for admission; keeps the grade 12 details. */
@Getter
@Setter
@Entity
@Table(name = "applicants")
public class Applicant extends SoftDeletableEntity {

    /** Personal details (people module). */
    @Column(name = "person_id", nullable = false)
    private Long personId;

    /** The login account (security module), if the applicant has one. */
    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "high_school")
    private String highSchool;

    @Column(name = "grade12_year")
    private Integer grade12Year;

    @Column(name = "grade12_result")
    private String grade12Result;
}
