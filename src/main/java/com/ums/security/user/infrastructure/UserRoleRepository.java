package com.ums.security.user.infrastructure;

import com.ums.security.user.domain.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Whether the user already has this role, in any scope.
     * <pre>
     * SELECT ur.id FROM user_roles ur WHERE ur.user_id = ? AND ur.role_id = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param userId id of the user
     * @param roleId id of the role
     * @return true when the user has the role
     */
    boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

    /**
     * Whether the user already has this role in this exact scope.
     * <pre>
     * SELECT ur.id FROM user_roles ur
     * WHERE ur.user_id = ? AND ur.role_id = ? AND ur.scope_type = ? AND ur.scope_id = ?
     * FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param userId    id of the user
     * @param roleId    id of the role
     * @param scopeType GLOBAL, FACULTY, DEPARTMENT, PROGRAM or CLASS_SECTION
     * @param scopeId   id of the row in that table (null for GLOBAL)
     * @return true when the user has the role in that scope
     */
    boolean existsByUser_IdAndRole_IdAndScopeTypeAndScopeId(Long userId, Long roleId, String scopeType, Long scopeId);

    /**
     * The roles given to one user, oldest first.
     * <pre>
     * SELECT ur.* FROM user_roles ur WHERE ur.user_id = ? ORDER BY ur.id ASC
     * </pre>
     *
     * @param userId id of the user
     * @return the user's role assignments
     */
    List<UserRole> findByUser_IdOrderByIdAsc(Long userId);

    /**
     * How many users have this role.
     * <pre>
     * SELECT count(ur.id) FROM user_roles ur WHERE ur.role_id = ?
     * </pre>
     *
     * @param roleId id of the role
     * @return number of assignments of the role
     */
    long countByRole_Id(Long roleId);

    /**
     * How many users have the role with this code.
     * <pre>
     * SELECT count(ur.id) FROM user_roles ur
     * JOIN roles r ON r.id = ur.role_id
     * WHERE r.code = ?
     * </pre>
     *
     * @param roleCode role code, e.g. SUPER_ADMIN
     * @return number of assignments of the role
     */
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
