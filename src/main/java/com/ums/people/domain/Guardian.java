package com.ums.people.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A parent or other guardian of one or more students. */
@Getter
@Setter
@Auditable
@Entity
@Table(name = "guardians")
public class Guardian extends SoftDeletableEntity {

    /** The login account (security module); null when the guardian has no login. */
    @Column(name = "user_id", unique = true)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "occupation")
    private String occupation;
}
