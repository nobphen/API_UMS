package com.ums.security.role.infrastructure;

import com.ums.security.role.domain.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCodeIgnoreCase(String code);

    /** System roles first, then the ones admins made. */
    List<Role> findAllByOrderBySystemDescNameAsc();
}
