package com.ums.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** Base for entities that are soft deleted: deleting only fills deleted_at, the row stays in the table. */
@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /** True when the row is soft deleted (deleted_at is set). */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
