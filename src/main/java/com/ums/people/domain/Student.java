package com.ums.people.domain;

import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student extends SoftDeletableEntity {

    /** The login account (security module). One login can own several student rows: Bachelor, then Master, or a double degree. */
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    /** The application that admitted the student into this program; empty when the registrar added them directly. */
    @Column(name = "application_id", unique = true)
    private Long applicationId;

    @Column(name = "student_code", nullable = false, unique = true)
    private String studentCode;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "batch_id")
    private Long batchId;

    /** Current year of study (1 = first year); the registrar moves it up each year. */
    @Column(name = "year_level", nullable = false)
    private int yearLevel = 1;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "expected_graduation_year")
    private Integer expectedGraduationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE, SUSPENDED, ON_LEAVE, GRADUATED, DROPPED;

        /** Still studying the program: blocks a second current record for the same program. */
        public static final Set<Status> CURRENT = EnumSet.of(ACTIVE, SUSPENDED, ON_LEAVE);
    }
}
