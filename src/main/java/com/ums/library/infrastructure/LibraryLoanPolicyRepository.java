package com.ums.library.infrastructure;

import com.ums.library.domain.LibraryLoanPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryLoanPolicyRepository extends JpaRepository<LibraryLoanPolicy, Long> {

    /**
     * The loan rules for one kind of borrower.
     * <pre>
     * SELECT lp.* FROM library_loan_policies lp WHERE lp.borrower_type = ?
     * </pre>
     *
     * @param borrowerType UNDERGRADUATE, POSTGRADUATE, LECTURER or STAFF
     * @return the policy, or empty when none is set for this borrower type
     */
    Optional<LibraryLoanPolicy> findByBorrowerType(LibraryLoanPolicy.BorrowerType borrowerType);
}
