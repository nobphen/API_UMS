package com.ums.finance.fee.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A kind of charge: tuition, admission fee, transcript fee, ... */
@Getter
@Setter
@Entity
@Table(name = "fee_types")
public class FeeType extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_refundable", nullable = false)
    private boolean refundable = false;
}
