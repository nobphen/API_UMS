package com.ums.security.user.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.common.util.PhoneNumbers;
import com.ums.security.auth.application.PermissionCacheService;
import com.ums.security.role.application.RoleService;
import com.ums.security.role.domain.Role;
import com.ums.security.role.infrastructure.RoleRepository;
import com.ums.security.session.application.UserSessionService;
import com.ums.security.session.domain.UserSession;
import com.ums.security.user.api.dto.UserRoleRequest;
import com.ums.security.user.api.dto.UserRoleResponse;
import com.ums.security.user.application.UserService;
import com.ums.security.user.application.mapper.UserMapper;
import com.ums.security.user.domain.User;
import com.ums.security.user.domain.UserRole;
import com.ums.security.user.infrastructure.UserRepository;
import com.ums.security.user.infrastructure.UserRoleRepository;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login accounts made by staff (not sign-up): new accounts with a temporary password, contact changes,
 * password reset, lock / unlock, language, and the roles of a user.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // No 0/O, 1/l/I: the password is read off a printed slip
    private static final String LETTERS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGITS = "23456789";
    private static final int TEMP_PASSWORD_LENGTH = 10;
    private static final String STUDENT_ROLE = "STUDENT";
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final String GLOBAL = "GLOBAL";

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionService userSessionService;
    private final PermissionCacheService permissionCacheService;
    private final UserMapper userMapper;

    /**
     * Username, email and phone must be free → random temporary password (saved only as a hash)
     * → INSERT the user (mustChangePassword = true) → give the role.
     */
    @Override
    public NewAccount createAccount(String username, String email, String phone, User.UserType userType, String roleCode) {

        // 1-3. Username, email and phone must be free (see checkNewAccount)
        checkNewAccount(username, email, phone);
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = PhoneNumbers.normalizeForSave(phone);   // the phone is also a login name

        // 4. The role (STUDENT, LECTURER ...) comes from the seed data; missing = a setup mistake, not a user mistake
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Role " + roleCode + " is missing; check V3__seed_roles_and_admin.sql"));

        // 5. INSERT the user with a random temporary password; only its hash is saved, never the password itself
        String temporaryPassword = temporaryPassword();
        User user = new User();
        user.setUsername(username);
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setUserType(userType);
        user.setMustChangePassword(true);   // the first login asks for a new password
        userRepository.save(user);          // after save() the user has its id

        // 6. Give the role: one user_roles row (user ↔ role)
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        // 7. Answer with the plain temporary password: this is the only time anyone can see it
        return new NewAccount(user.getId(), user.getUsername(), temporaryPassword);
    }

    /** The checks of createAccount only: username, email and phone must be free. Saves nothing. */
    @Override
    public void checkNewAccount(String username, String email, String phone) {

        // 1. The username must be free (for a student it is the student code)
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("error.user.username_taken", username);
        }

        // 2. The email must be free: it is used for "Forgot password?" and can be typed to log in
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail != null && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("error.user.email_taken");
        }

        // 3. The phone must be free too (normalizeForSave also refuses a bad phone: error.phone.invalid)
        String normalizedPhone = PhoneNumbers.normalizeForSave(phone);
        if (normalizedPhone != null && userRepository.existsByPhone(normalizedPhone)) {
            throw new BusinessException("error.user.phone_taken");
        }
    }

    /** Refuses a LOCKED login, switches it ACTIVE, adds the STUDENT role if missing (and clears its permission cache). */
    @Override
    public String useForNewStudy(Long userId) {

        // 1. The login must exist (404) and not be locked by an admin
        User user = getUser(userId);
        if (user.getStatus() == User.Status.LOCKED) {
            throw new BusinessException("error.user.locked_unlock_first");
        }

        // 2. Switch it on again and set its type
        user.setStatus(User.Status.ACTIVE);   // DISABLED after graduating → in use again
        // Only an applicant turns into a student. A lecturer or guardian who studies keeps their main type;
        // the STUDENT role below gives them the student permissions as well.
        if (user.getUserType() == User.UserType.APPLICANT) {
            user.setUserType(User.UserType.STUDENT);
        }

        // 3. Add the STUDENT role, only if the user does not have it yet
        Role studentRole = roleRepository.findByCode(STUDENT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role STUDENT is missing; check V3__seed_roles_and_admin.sql"));

        if (!userRoleRepository.existsByUser_IdAndRole_Id(userId, studentRole.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(studentRole);
            userRoleRepository.save(userRole);
            permissionCacheService.evict(userId);   // forget the old permissions, so the student ones work at once
        }

        // 4. Same login as before: answer with its username (no new password)
        return user.getUsername();
    }

    /** Same email / phone rules as createAccount, but the user's own current values don't count as taken. */
    @Override
    public void updateContact(Long userId, String email, String phone) {
        User user = getUser(userId);   // 404 when it does not exist

        // 1. Email: "" → null, "A@B.com" → "a@b.com"; nobody else may have it
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail != null && userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, userId)) {
            throw new BusinessException("error.user.email_taken");
        }

        // 2. Phone: saved as +855...; nobody else may have it
        String normalizedPhone = PhoneNumbers.normalizeForSave(phone);
        if (normalizedPhone != null && userRepository.existsByPhoneAndIdNot(normalizedPhone, userId)) {
            throw new BusinessException("error.user.phone_taken");
        }

        // 3. No save(): the user was loaded in this transaction, so JPA writes the UPDATE at commit
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
    }

    /** Username is not oldUsername → nothing; otherwise the new one must be free (unless only the letter case changes) → set it. */
    @Override
    public boolean renameIfUsername(Long userId, String oldUsername, String newUsername) {
        User user = getUser(userId);   // 404 when it does not exist

        // 1. Only a login still named after the old code (a lecturer keeps their own username)
        if (!user.getUsername().equalsIgnoreCase(oldUsername)) {
            return false;
        }

        // 2. Nobody else may have the new username ("st2026001" → "ST2026001" is the same login, no check)
        if (!newUsername.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsernameIgnoreCase(newUsername)) {
            throw new BusinessException("error.user.username_taken", newUsername);
        }

        // 3. No save(): JPA writes the UPDATE at commit. Old tokens keep working; the next login uses the new username
        user.setUsername(newUsername);
        return true;
    }

    /** New temporary password (hash saved, must change at login), wrong-password count back to 0, all sessions ended. */
    @Override
    public String resetPassword(Long userId, Long adminId) {
        User user = getUser(userId);
        String temporaryPassword = temporaryPassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setFailedLoginCount(0);
        userSessionService.revokeAll(userId, UserSession.RevokedReason.ADMIN, adminId, null);
        return temporaryPassword;
    }

    /** An admin can't lock themselves; status → LOCKED and every session ends as USER_LOCKED. */
    @Override
    public void lock(Long userId, Long adminId) {
        if (userId.equals(adminId)) {
            throw new BusinessException("error.user.lock_self");
        }
        User user = getUser(userId);
        user.setStatus(User.Status.LOCKED);
        userSessionService.revokeAll(userId, UserSession.RevokedReason.USER_LOCKED, adminId, null);
    }

    /** LOCKED → ACTIVE (other statuses stay) and the wrong-password count back to 0. */
    @Override
    public void unlock(Long userId) {
        User user = getUser(userId);
        if (user.getStatus() == User.Status.LOCKED) {
            user.setStatus(User.Status.ACTIVE);
        }
        user.setFailedLoginCount(0);
    }

    /** Loads the user and sets the language; JPA writes the UPDATE at commit. */
    @Override
    public void setPreferredLanguage(Long userId, String language) {
        getUser(userId).setPreferredLanguage(language);
    }

    /// ROLES OF A USER ----------------------------------------------

    /** Checks the user exists (404), then reads their user_roles rows, oldest first. */
    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getRoles(Long userId) {
        getUser(userId);
        return userRoleRepository.findByUser_IdOrderByIdAsc(userId).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Caller may give the role → role is ACTIVE → scope fits (GLOBAL has no scopeId, others need one)
     * → dates in order → not given twice → INSERT, then clear the user's permission cache.
     */
    @Override
    public UserRoleResponse assignRole(Long userId, UserRoleRequest request, Set<String> callerAuthorities) {
        User user = getUser(userId);
        Role role = roleService.requireGivable(request.roleCode(), callerAuthorities);
        if (!"ACTIVE".equals(role.getStatus())) {
            throw new BusinessException("error.role.inactive", role.getCode());
        }

        String scopeType = request.scopeType() == null ? GLOBAL : request.scopeType();
        if (GLOBAL.equals(scopeType) != (request.scopeId() == null)) {
            throw GLOBAL.equals(scopeType)
                    ? new BusinessException("error.user_role.global_no_scope")
                    : new BusinessException("error.user_role.scope_required", new DefaultMessageSourceResolvable("scope." + scopeType));
        }
        if (request.validFrom() != null && request.validTo() != null && request.validTo().isBefore(request.validFrom())) {
            throw new BusinessException("error.user_role.dates");
        }
        if (userRoleRepository.existsByUser_IdAndRole_IdAndScopeTypeAndScopeId(userId, role.getId(), scopeType, request.scopeId())) {
            throw new BusinessException("error.user_role.duplicate");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setScopeType(scopeType);
        userRole.setScopeId(request.scopeId());
        userRole.setValidFrom(request.validFrom());
        userRole.setValidTo(request.validTo());
        userRoleRepository.save(userRole);
        permissionCacheService.evict(userId);
        return userMapper.toResponse(userRole);
    }

    /** The row must belong to this user (404) → caller may take the role away → not the last SUPER_ADMIN → DELETE. */
    @Override
    public void removeRole(Long userId, Long userRoleId, Set<String> callerAuthorities) {
        UserRole userRole = userRoleRepository.findById(userRoleId)
                .filter(ur -> ur.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("error.user_role.not_found"));
        String roleCode = userRole.getRole().getCode();
        roleService.requireGivable(roleCode, callerAuthorities);
        if (SUPER_ADMIN_ROLE.equals(roleCode) && userRoleRepository.countByRole_Code(SUPER_ADMIN_ROLE) <= 1) {
            throw new BusinessException("error.user_role.last_super_admin");
        }
        userRoleRepository.delete(userRole);
        permissionCacheService.evict(userId);
    }

    /** The user, or 404 when it does not exist. */
    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("error.user.not_found"));
    }

    /** One way to save an email: "" → null, " A@B.com " → "a@b.com". */
    private static String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }

    /** Always has a letter and a digit, so it passes the same rules as a password the user picks. */
    private String temporaryPassword() {
        String all = LETTERS + DIGITS;
        String password;
        do {
            StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
            for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
                sb.append(all.charAt(random.nextInt(all.length())));
            }
            password = sb.toString();
        } while (password.chars().noneMatch(Character::isLetter) || password.chars().noneMatch(Character::isDigit));
        return password;
    }
}
