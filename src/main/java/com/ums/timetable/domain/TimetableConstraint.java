package com.ums.timetable.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A rule the timetable generator must (hard) or should (soft) follow in a semester. */
@Getter
@Setter
@Entity
@Table(name = "timetable_constraints")
public class TimetableConstraint extends BaseEntity {

    /** The semester (academic module). */
    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "constraint_type", nullable = false)
    private ConstraintType constraintType;

    /** What the rule applies to (e.g. LECTURER, ROOM, BATCH), together with scopeId; null = everything. */
    @Column(name = "scope_type")
    private String scopeType;

    @Column(name = "scope_id")
    private Long scopeId;

    /** Rule parameters as JSON, e.g. the max hours or the preferred room. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value")
    private String value;

    /** true = must never be broken; false = soft rule, broken at the cost of its weight. */
    @Column(name = "is_hard", nullable = false)
    private boolean hard = true;

    @Column(name = "weight", nullable = false)
    private int weight = 1;

    public enum ConstraintType {
        NO_ROOM_CLASH, NO_LECTURER_CLASH, NO_BATCH_CLASH, MAX_HOURS_PER_DAY, PREFERRED_ROOM, BREAK_TIME
    }
}
