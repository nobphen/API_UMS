package com.ums.asset.domain;

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
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "assets")
public class Asset extends BaseEntity {

    @Column(name = "asset_code", nullable = false, unique = true)
    private String assetCode;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private AssetCategory category;

    @Column(name = "serial_no")
    private String serialNo;

    /** Where the asset is (facility module). */
    @Column(name = "room_id")
    private Long roomId;

    /** The department that owns the asset (organization module). */
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost")
    private BigDecimal purchaseCost;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.IN_USE;

    public enum Status { IN_USE, IN_STORE, UNDER_REPAIR, DISPOSED }
}
