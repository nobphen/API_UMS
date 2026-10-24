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
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** A member of a thesis defense committee: a staff member, or an external examiner. */
@Getter
@Setter
@Entity
@Table(name = "thesis_committee_members")
public class ThesisCommitteeMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thesis_id")
    private Thesis thesis;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "external_name")
    private String externalName;

    @Column(name = "external_institution")
    private String externalInstitution;

    @Enumerated(EnumType.STRING)
    @Column(name = "committee_role", nullable = false)
    private Role role;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "comment")
    private String comment;

    public enum Role { CHAIR, EXAMINER, EXTERNAL_EXAMINER, SECRETARY }
}
