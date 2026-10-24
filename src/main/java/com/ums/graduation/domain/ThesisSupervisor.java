package com.ums.graduation.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A supervisor of a thesis: a staff member, or someone from another university (externalName). */
@Getter
@Setter
@Entity
@Table(name = "thesis_supervisors")
public class ThesisSupervisor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thesis_id")
    private Thesis thesis;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "external_name")
    private String externalName;

    @Column(name = "external_institution")
    private String externalInstitution;

    /** One MAIN supervisor per thesis, any number of co-supervisors. */
    @Enumerated(EnumType.STRING)
    @Column(name = "supervisor_role", nullable = false)
    private Role role = Role.MAIN;

    public enum Role { MAIN, CO_SUPERVISOR }
}
