package com.ums.asset.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** An asset handed to a staff member; an asset has at most one open assignment (returnedAt null). */
@Getter
@Setter
@Entity
@Table(name = "asset_assignments")
public class AssetAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    /** The staff member holding the asset (people module). */
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDate assignedAt = LocalDate.now();

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Column(name = "condition_note")
    private String conditionNote;
}
