package com.ums.security.role.infrastructure;

import com.ums.security.role.domain.Permission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Every permission, grouped by module.
     * <pre>
     * SELECT p.* FROM permissions p ORDER BY p.module ASC, p.code ASC
     * </pre>
     *
     * @return all permissions, ordered by module, then code
     */
    List<Permission> findAllByOrderByModuleAscCodeAsc();

    /**
     * The permissions with these codes.
     * <pre>
     * SELECT p.* FROM permissions p WHERE p.code IN (?, ?, ...)
     * </pre>
     *
     * @param codes permission codes, e.g. STUDENT_VIEW
     * @return the permissions found; codes that don't exist are left out
     */
    List<Permission> findByCodeIn(Collection<String> codes);
}
