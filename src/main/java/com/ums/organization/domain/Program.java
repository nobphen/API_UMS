package com.ums.organization.domain;

import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** A degree program offered by a department, e.g. Bachelor of Computer Science. */
@Getter
@Setter
@Entity
@Table(name = "programs")
public class Program extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_kh")
    private String nameKh;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_level", nullable = false)
    private DegreeLevel degreeLevel;

    /** Normal length of study in years, e.g. 4.0 or 1.5. */
    @Column(name = "duration_years", nullable = false)
    private BigDecimal durationYears;

    /** Credits needed to graduate. */
    @Column(name = "total_credits", nullable = false)
    private int totalCredits;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public enum DegreeLevel { FOUNDATION, CERTIFICATE, DIPLOMA, ASSOCIATE, BACHELOR, MASTER, PHD }
}
