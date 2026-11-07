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
     *
     * @param username e.g. the student code ST2026001
     * @param email    optional; must be free when given
     * @param phone    optional, as typed; saved normalized (+855...); must be free when given
     * @param userType STUDENT, STAFF, GUARDIAN...
     * @param roleCode role given straight away, e.g. STUDENT
     * @return the user id, username and the temporary password (show it once, then it is gone)
     * @throws com.ums.common.exception.BusinessException error.user.username_taken / email_taken / phone_taken;
     *                                                    error.phone.invalid
     */
    NewAccount createAccount(String username, String email, String phone, User.UserType userType, String roleCode);

    /**
     * Only the checks of createAccount, saves nothing. Call it before saving anything else (person, addresses ...),
     * so a taken username, email or phone stops the request before any INSERT (no lost ids).
     *
     * @param username e.g. the student code ST2026001
     * @param email    optional; must be free when given
     * @param phone    optional, as typed; must be free when given
     * @throws com.ums.common.exception.BusinessException error.user.username_taken / email_taken / phone_taken;
     *                                                    error.phone.invalid
     */
    void checkNewAccount(String username, String email, String phone);

    /**
     * An existing login starts another program (e.g. a Bachelor graduate begins a Master's, or a lecturer studies
     * a PhD): it gets the STUDENT role, and is switched back on if it was disabled after graduating. An APPLICANT
     * login becomes a STUDENT login; staff and guardians keep their type.
     *
     * @param userId the existing login
     * @return the username, which does not change
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     * @throws com.ums.common.exception.BusinessException error.user.locked_unlock_first when the login is LOCKED
     */
    String useForNewStudy(Long userId);

    /**
     * An admin (e.g. the registrar) changes the login email and phone, so "Forgot password?" and SMS login
     * use the new ones. Same checks as createAccount: each must be free.
     * A user changing their own email should prove it with a code first (not this method).
     *
     * @param userId the login
     * @param email  new email; empty = removed
     * @param phone  new phone, as typed; empty = removed
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     * @throws com.ums.common.exception.BusinessException error.user.email_taken / phone_taken when another login has it;
     *                                                    error.phone.invalid
     */
    void updateContact(Long userId, String email, String phone);

    /**
     * Renames the login, but only when its username is still oldUsername (e.g. the student code typed wrong).
     * A login with another username (a lecturer's, or the code of an earlier program) is left alone.
     *
     * @param userId      the login
     * @param oldUsername the username it should have now (letter case ignored)
     * @param newUsername the new username
     * @return true when it was renamed, false when its username was something else
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     * @throws com.ums.common.exception.BusinessException error.user.username_taken when another login has it
     */
    boolean renameIfUsername(Long userId, String oldUsername, String newUsername);

    /**
     * Admin gives a new temporary password (e.g. at the counter); all the user's sessions end.
     * For users with no email, who can't use "Forgot password?".
     *
     * @param userId  the login (users.id, not students.id)
     * @param adminId the admin doing it, saved as who ended the sessions
     * @return the new temporary password (show it once)
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     */
    String resetPassword(Long userId, Long adminId);

    /**
     * All the user's sessions end, and they can't log in until unlocked.
     *
     * @param userId  the login to lock
     * @param adminId the admin doing it
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     * @throws com.ums.common.exception.BusinessException error.user.lock_self when the admin locks themselves
     */
    void lock(Long userId, Long adminId);

    /**
     * LOCKED → ACTIVE. Also clears the wrong-password count that locks an account after 5 tries.
     *
     * @param userId the login to unlock
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     */
    void unlock(Long userId);

    /**
     * The app gets it in the next access token; notifications and emails use it straight away.
     *
     * @param userId   the login
     * @param language "km" or "en"
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     */
    void setPreferredLanguage(Long userId, String language);

    /// ROLES OF A USER (takes effect at the user's next token refresh, at most 15 minutes) ----------

    /**
     * The roles the user holds (with scope and dates), oldest first.
     *
     * @param userId the login
     * @return the role assignments; empty when none
     * @throws com.ums.common.exception.NotFoundException error.user.not_found
     */
    List<UserRoleResponse> getRoles(Long userId);

    /**
     * Gives a role, for the whole university (GLOBAL) or one faculty, department, program or class section.
     * Only a SUPER_ADMIN can hand out SUPER_ADMIN, and nobody can hand out a role that has permissions
     * they don't hold themselves.
     *
     * @param userId            the login
     * @param request           roleCode, scopeType + scopeId, validFrom / validTo
     * @param callerAuthorities the admin's own authorities (from their token)
     * @return the new assignment
     * @throws com.ums.common.exception.NotFoundException error.user.not_found; error.role.not_found_code
     * @throws com.ums.common.exception.BusinessException error.role.super_admin_only / error.role.cannot_grant;
     *                                                    error.role.inactive;
     *                                                    error.user_role.global_no_scope / scope_required;
     *                                                    error.user_role.dates; error.user_role.duplicate
     */
    UserRoleResponse assignRole(Long userId, UserRoleRequest request, Set<String> callerAuthorities);

    /**
     * Takes a role away. The last SUPER_ADMIN can't be removed, so the system is never left without one.
     *
     * @param userId            the login
     * @param userRoleId        id of the assignment (from getRoles), not the role id
     * @param callerAuthorities the admin's own authorities (from their token)
     * @throws com.ums.common.exception.NotFoundException error.user_role.not_found when it isn't this user's
     * @throws com.ums.common.exception.BusinessException error.role.super_admin_only / error.role.cannot_grant;
     *                                                    error.user_role.last_super_admin
     */
    void removeRole(Long userId, Long userRoleId, Set<String> callerAuthorities);

    /** The result of createAccount; temporaryPassword is shown once and never stored in plain text. */
    record NewAccount(Long userId, String username, String temporaryPassword) {
    }
}
