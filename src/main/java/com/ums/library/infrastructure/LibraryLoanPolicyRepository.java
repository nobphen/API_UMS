package com.ums.library.infrastructure;

import com.ums.library.domain.LibraryLoanPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryLoanPolicyRepository extends JpaRepository<LibraryLoanPolicy, Long> {

    Optional<LibraryLoanPolicy> findByBorrowerType(LibraryLoanPolicy.BorrowerType borrowerType);
}
