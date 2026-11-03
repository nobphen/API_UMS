package com.ums.transport.domain;

import com.ums.common.audit.Auditable;
import com.ums.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Auditable
@Entity
@Table(name = "transport_routes")
public class TransportRoute extends SoftDeletableEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_kh")
    private String nameKh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "morning_departure")
    private LocalTime morningDeparture;

    @Column(name = "evening_departure")
    private LocalTime eveningDeparture;

    @Column(name = "fee_per_semester", nullable = false)
    private BigDecimal feePerSemester = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
}
