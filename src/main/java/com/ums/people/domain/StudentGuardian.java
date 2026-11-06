package com.ums.people.domain;

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

/** Links a student to a guardian (parent), with the relationship and whether this guardian is contacted first. */
@Getter
@Setter
@Entity
@Table(name = "student_guardians")
public class StudentGuardian {

    @EmbeddedId
    private Id id;

    /** FATHER, MOTHER, UNCLE, ... */
    @Column(name = "relationship", nullable = false)
    private String relationship;

    /** The guardian contacted first. */
    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    /** The key: student id + guardian id (one row per student-guardian pair). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class Id implements Serializable {

        @Column(name = "student_id")
        private Long studentId;

        @Column(name = "guardian_id")
        private Long guardianId;
    }
}
