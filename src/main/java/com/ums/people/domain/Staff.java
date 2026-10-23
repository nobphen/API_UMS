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
import lombok.Getter;
import lombok.Setter;

/** Everyone who works at the university, teachers included (staffType LECTURER or BOTH). */
@Getter
@Setter
@Entity
@Table(name = "staff")
public class Staff extends SoftDeletableEntity {

    /** The login account (security module). */
    @Column(name = "user_id", unique = true)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "staff_code", nullable = false, unique = true)
    private String staffCode;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "position")
    private String position;

    /** Dr., Prof., Assoc. Prof. */
    @Column(name = "academic_title")
    private String academicTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false)
    private StaffType staffType;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    /** What the person does; what they may do in the system comes from their roles. */
    public enum StaffType { LECTURER, OFFICE, BOTH }

    public enum Status { ACTIVE, ON_LEAVE, RESIGNED, TERMINATED, RETIRED }
}
