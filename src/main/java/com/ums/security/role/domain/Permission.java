package com.ums.security.role.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    /** e.g. STUDENT_VIEW; matches the constants in each module's *Permissions class. */
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "module", nullable = false)
    private String module;

    /** VIEW, CREATE, UPDATE, DELETE, APPROVE, EXPORT or MANAGE. */
    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "description")
    private String description;

    @Column(name = "description_kh")
    private String descriptionKh;
}
