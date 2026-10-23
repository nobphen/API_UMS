package com.ums.security.user.domain;

import com.ums.common.entity.BaseEntity;
import com.ums.security.role.domain.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_roles")
public class UserRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    /** GLOBAL, FACULTY, DEPARTMENT, PROGRAM or CLASS_SECTION; scopeId points into that table. */
    @Column(name = "scope_type", nullable = false)
    private String scopeType = "GLOBAL";

    @Column(name = "scope_id")
    private Long scopeId;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;
}
