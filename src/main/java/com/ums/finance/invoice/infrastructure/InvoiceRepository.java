package com.ums.finance.invoice.infrastructure;

import com.ums.finance.invoice.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
