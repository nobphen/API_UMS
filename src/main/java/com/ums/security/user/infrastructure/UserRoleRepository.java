package com.ums.security.user.infrastructure;

import com.ums.security.user.domain.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

    boolean existsByUser_IdAndRole_IdAndScopeTypeAndScopeId(Long userId, Long roleId, String scopeType, Long scopeId);

    List<UserRole> findByUser_IdOrderByIdAsc(Long userId);

    long countByRole_Id(Long roleId);

    long countByRole_Code(String roleCode);

    /** Users whose cached permissions must be dropped when the role changes. */
    @Query("select distinct ur.user.id from UserRole ur where ur.role.id = :roleId")
    List<Long> findUserIdsByRoleId(Long roleId);

    /** Codes of the user's active roles whose validity period includes today. */
    @Query("""
            select distinct r.code from UserRole ur join ur.role r
            where ur.user.id = :userId and r.status = 'ACTIVE'
              and (ur.validFrom is null or ur.validFrom <= current_date)
              and (ur.validTo is null or ur.validTo >= current_date)
            """)
    List<String> findActiveRoleCodes(Long userId);

    /** Permission codes granted by those same roles. */
    @Query("""
            select distinct p.code from UserRole ur join ur.role r join r.permissions p
            where ur.user.id = :userId and r.status = 'ACTIVE'
              and (ur.validFrom is null or ur.validFrom <= current_date)
              and (ur.validTo is null or ur.validTo >= current_date)
            """)
    List<String> findActivePermissionCodes(Long userId);
}
