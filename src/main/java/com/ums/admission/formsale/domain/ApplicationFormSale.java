package com.ums.admission.formsale.domain;

import com.ums.admission.intake.domain.AdmissionIntake;
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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/** An application form sold at the counter, before the buyer has applied. */
@Getter
@Setter
@Entity
@Table(name = "application_form_sales")
public class ApplicationFormSale extends BaseEntity {

    @Column(name = "form_no", nullable = false, unique = true)
    private String formNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intake_id")
    private AdmissionIntake intake;

    @Column(name = "buyer_name", nullable = false)
    private String buyerName;

    @Column(name = "buyer_phone")
    private String buyerPhone;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /** The walk-in payment (finance module). */
    @Column(name = "payment_id")
    private Long paymentId;

    /** The login account of the staff member who sold the form. */
    @Column(name = "sold_by")
    private Long soldBy;

    @Column(name = "sold_at", nullable = false)
    private OffsetDateTime soldAt = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.SOLD;

    /** USED: an application was made with it. */
    public enum Status { SOLD, USED, VOID, REFUNDED }
}
