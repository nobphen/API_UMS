package com.ums.request.leave.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Kind of student leave: sick, family, ... */
@Getter
@Setter
@Entity
@Table(name = "leave_types")
public class LeaveType extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_kh")
    private String nameKh;

    /** A supporting document (e.g. medical certificate) must be attached. */
    @Column(name = "requires_document", nullable = false)
    private boolean requiresDocument = false;

    /** Longest leave allowed per request; null = no limit. */
    @Column(name = "max_days")
    private Integer maxDays;
}
