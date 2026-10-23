package com.ums.security.user.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public NewAccount createAccount(String username, String email, String phone, User.UserType userType, String roleCode) {
        String normalizedEmail = email == null || email.isBlank() ? null : email.trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("Username is already taken: " + username);
        }
        if (normalizedEmail != null && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("Email is already used by another account");
        }
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Role " + roleCode + " is missing; check V3__seed_roles_and_admin.sql"));

        String temporaryPassword = temporaryPassword();
        User user = new User();
        user.setUsername(username);
        user.setEmail(normalizedEmail);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setUserType(userType);
        user.setMustChangePassword(true);
        userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        return new NewAccount(user.getId(), user.getUsername(), temporaryPassword);
    }

    @Override
    public String useForNewStudy(Long userId) {
        User user = getUser(userId);
        if (user.getStatus() == User.Status.LOCKED) {
            throw new BusinessException("This login is locked; unlock it first");
        }
        user.setStatus(User.Status.ACTIVE);   // DISABLED after graduating → in use again
        // Only an applicant turns into a student. A lecturer or guardian who studies keeps their main type;
        // the STUDENT role below gives them the student permissions as well.
        if (user.getUserType() == User.UserType.APPLICANT) {
            user.setUserType(User.UserType.STUDENT);
        }
        Role studentRole = roleRepository.findByCode(STUDENT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role STUDENT is missing; check V3__seed_roles_and_admin.sql"));
        if (!userRoleRepository.existsByUser_IdAndRole_Id(userId, studentRole.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(studentRole);
            userRoleRepository.save(userRole);
            permissionCacheService.evict(userId);
        }
        return user.getUsername();
    }

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

    @Override
    public void lock(Long userId, Long adminId) {
        if (userId.equals(adminId)) {
            throw new BusinessException("You can't lock your own account");
        }
        User user = getUser(userId);
        user.setStatus(User.Status.LOCKED);
        userSessionService.revokeAll(userId, UserSession.RevokedReason.USER_LOCKED, adminId, null);
    }

    @Override
    public void unlock(Long userId) {
        User user = getUser(userId);
        if (user.getStatus() == User.Status.LOCKED) {
            user.setStatus(User.Status.ACTIVE);
        }
        user.setFailedLoginCount(0);
    }

    /// ROLES OF A USER ----------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getRoles(Long userId) {
        getUser(userId);
        return userRoleRepository.findByUser_IdOrderByIdAsc(userId).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserRoleResponse assignRole(Long userId, UserRoleRequest request, Set<String> callerAuthorities) {
        User user = getUser(userId);
        Role role = roleService.requireGivable(request.roleCode(), callerAuthorities);
        if (!"ACTIVE".equals(role.getStatus())) {
            throw new BusinessException("Role " + role.getCode() + " is inactive");
        }

        String scopeType = request.scopeType() == null ? GLOBAL : request.scopeType();
        if (GLOBAL.equals(scopeType) != (request.scopeId() == null)) {
            throw new BusinessException(GLOBAL.equals(scopeType)
                    ? "A GLOBAL role has no scopeId"
                    : "scopeId is needed: the id of the " + scopeType.toLowerCase().replace('_', ' '));
        }
        if (request.validFrom() != null && request.validTo() != null && request.validTo().isBefore(request.validFrom())) {
            throw new BusinessException("validTo must be on or after validFrom");
        }
        if (userRoleRepository.existsByUser_IdAndRole_IdAndScopeTypeAndScopeId(userId, role.getId(), scopeType, request.scopeId())) {
            throw new BusinessException("The user already has this role here");
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

    @Override
    public void removeRole(Long userId, Long userRoleId, Set<String> callerAuthorities) {
        UserRole userRole = userRoleRepository.findById(userRoleId)
                .filter(ur -> ur.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("The user does not have this role"));
        String roleCode = userRole.getRole().getCode();
        roleService.requireGivable(roleCode, callerAuthorities);
        if (SUPER_ADMIN_ROLE.equals(roleCode) && userRoleRepository.countByRole_Code(SUPER_ADMIN_ROLE) <= 1) {
            throw new BusinessException("This is the last SUPER_ADMIN; give the role to someone else first");
        }
        userRoleRepository.delete(userRole);
        permissionCacheService.evict(userId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
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
