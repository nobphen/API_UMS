package com.ums.asset.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "asset_maintenance")
public class AssetMaintenance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "vendor")
    private String vendor;

    /** When the next service is due; null = none planned. */
    @Column(name = "next_due_date")
    private LocalDate nextDueDate;
}
