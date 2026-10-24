package com.ums.security.role.infrastructure;

import com.ums.security.role.domain.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * The role with this exact code (case matters).
     * <pre>
     * SELECT r.* FROM roles r WHERE r.code = ?
     * </pre>
     *
     * @param code role code, e.g. REGISTRAR
     * @return the role, or empty when no role has this code
     */
    Optional<Role> findByCode(String code);

    /**
     * Whether a role already uses this code, ignoring case, so REGISTRAR and registrar count as the same.
     * <pre>
     * SELECT r.id FROM roles r WHERE upper(r.code) = upper(?) FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param code role code to check
     * @return true when the code is taken
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * System roles first, then the ones admins made.
     * <pre>
     * SELECT r.* FROM roles r ORDER BY r.is_system DESC, r.name ASC
     * </pre>
     *
     * @return every role, system roles first, then by name
     */
    List<Role> findAllByOrderBySystemDescNameAsc();
}
