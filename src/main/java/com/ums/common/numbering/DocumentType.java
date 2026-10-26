package com.ums.common.numbering;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** The kinds of documents that get a number, and the prefix each number starts with. */
@Getter
@RequiredArgsConstructor
public enum DocumentType {

    INVOICE("INV"),             // invoices.invoice_no
    PAYMENT("PAY"),             // payments.payment_no
    FORM_SALE("FRM"),           // application_form_sales.form_no
    APPLICATION("APP"),         // applications.application_no
    STUDENT_REQUEST("SRQ"),     // student_requests.request_no
    TRANSCRIPT_REQUEST("TRQ"),  // transcript_requests.request_no
    LEAVE_REQUEST("LRQ"),       // leave_requests.request_no
    CERTIFICATE("CER"),         // certificates.certificate_no
    CONTRACT("CON");            // employment_contracts.contract_no

    private final String prefix;
}
