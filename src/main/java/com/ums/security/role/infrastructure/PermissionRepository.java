package com.ums.security.role.infrastructure;

import com.ums.security.role.domain.Permission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findAllByOrderByModuleAscCodeAsc();

    List<Permission> findByCodeIn(Collection<String> codes);
}
