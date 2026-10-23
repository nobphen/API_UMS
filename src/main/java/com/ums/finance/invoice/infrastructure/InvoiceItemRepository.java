package com.ums.finance.invoice.infrastructure;

import com.ums.finance.invoice.domain.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
}
