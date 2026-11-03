package com.ums.transport.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Auditable
@Entity
@Table(name = "vehicles")
public class Vehicle extends SoftDeletableEntity {

    @Column(name = "plate_no", nullable = false, unique = true)
    private String plateNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    /** Number of seats. */
    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public enum VehicleType { BUS, VAN }
}
