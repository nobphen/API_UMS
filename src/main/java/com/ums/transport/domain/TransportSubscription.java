package com.ums.transport.domain;

import com.ums.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A student riding a route for one semester; one subscription per student per semester. */
@Getter
@Setter
@Entity
@Table(name = "transport_subscriptions")
public class TransportSubscription extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id")
    private TransportRoute route;

    /** Where the student gets on. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id")
    private RouteStop stop;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    /** The finance invoice for the transport fee. */
    @Column(name = "invoice_id")
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    public enum Status { ACTIVE, CANCELLED }
}
