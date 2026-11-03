package com.ums.asset.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Auditable
@Entity
@Table(name = "asset_categories")
public class AssetCategory extends SoftDeletableEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_kh")
    private String nameKh;

    /** Useful life used for depreciation; null = not depreciated. */
    @Column(name = "depreciation_years")
    private Integer depreciationYears;
}
