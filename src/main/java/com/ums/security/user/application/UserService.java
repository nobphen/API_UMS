package com.ums.security.user.application;

import com.ums.security.user.api.dto.UserRoleRequest;
import com.ums.security.user.api.dto.UserRoleResponse;
import com.ums.security.user.domain.User;
import java.util.List;
import java.util.Set;
import org.springframework.modulith.NamedInterface;

/** Login accounts made by the office (students, staff, guardians) and what an admin can do to them. */
@NamedInterface("user-service")   // used by other modules
public interface UserService {

    /**
     * Creates an account with a random temporary password and the given role. The user must change the
     * password at first login. The temporary password is returned once and never stored in plain text.
     */
    NewAccount createAccount(String username, String email, String phone, User.UserType userType, String roleCode);

    /**
     * An existing login starts another program (e.g. a Bachelor graduate begins a Master's, or a lecturer studies
     * a PhD): it gets the STUDENT role, and is switched back on if it was disabled after graduating. An APPLICANT
     * login becomes a STUDENT login; staff and guardians keep their type.
     * Returns the username, which does not change.
     */
    String useForNewStudy(Long userId);

    /** Admin gives a new temporary password (e.g. at the counter); all the user's sessions end. */
    String resetPassword(Long userId, Long adminId);

    /** All the user's sessions end, and they can't log in until unlocked. */
    void lock(Long userId, Long adminId);

    /** Also clears the wrong-password count that locks an account after 5 tries. */
    void unlock(Long userId);

    /** "km" or "en". The app gets it in the next access token; notifications and emails use it straight away. */
    void setPreferredLanguage(Long userId, String language);

    /// ROLES OF A USER (takes effect at the user's next token refresh, at most 15 minutes) ----------

    List<UserRoleResponse> getRoles(Long userId);

    /**
     * callerAuthorities are the admin's own: only a SUPER_ADMIN can hand out SUPER_ADMIN, and nobody
     * can hand out a role that has permissions they don't hold themselves.
     */
    UserRoleResponse assignRole(Long userId, UserRoleRequest request, Set<String> callerAuthorities);

    /** The last SUPER_ADMIN can't be removed, so the system is never left without one. */
    void removeRole(Long userId, Long userRoleId, Set<String> callerAuthorities);

    record NewAccount(Long userId, String username, String temporaryPassword) {
    }
}
