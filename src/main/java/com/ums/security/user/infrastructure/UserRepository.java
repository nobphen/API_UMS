package com.ums.security.user.infrastructure;

import com.ums.common.util.PhoneNumbers;
import com.ums.security.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Login accepts the username, the email or the phone number, as typed ("012 345 678" finds +85512345678).
     *
     * @param login username, email or phone number
     * @return the user, or empty when nothing matches
     */
    default Optional<User> findByLogin(String login) {
        String value = login.trim();
        return findForLogin(value, PhoneNumbers.normalize(value));
    }

    /**
     * Use {@link #findByLogin(String)}, which fills in the phone. A null phone matches nobody.
     * <pre>
     * SELECT u.* FROM users u WHERE u.username = ? OR u.email = lower(?) OR u.phone = ?
     * </pre>
     *
     * @param login username or email
     * @param phone the same text as a normalized phone number, or null when it isn't one
     * @return the user, or empty when nothing matches
     */
    @Query("select u from User u where u.username = :login or u.email = lower(:login) or u.phone = :phone")
    Optional<User> findForLogin(String login, String phone);

    /**
     * Whether a user already has this phone number.
     * <pre>
     * SELECT u.id FROM users u WHERE u.phone = ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param phone normalized phone number, e.g. +85512345678
     * @return true when the number is taken
     */
    boolean existsByPhone(String phone);

    /**
     * The user with this phone number, for the SMS code login.
     * <pre>
     * SELECT u.* FROM users u WHERE u.phone = ?
     * </pre>
     *
     * @param phone normalized phone number, e.g. +85512345678
     * @return the user, or empty when no one has the number
     */
    Optional<User> findByPhone(String phone);

    /**
     * Whether a user already has this username, ignoring case.
     * <pre>
     * SELECT u.id FROM users u WHERE upper(u.username) = upper(?) FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param username username to check
     * @return true when the username is taken
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Whether a user already has this email, ignoring case.
     * <pre>
     * SELECT u.id FROM users u WHERE upper(u.email) = upper(?) FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param email email to check
     * @return true when the email is taken
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * The user with this email, ignoring case, for the email verification after sign-up.
     * <pre>
     * SELECT u.* FROM users u WHERE upper(u.email) = upper(?)
     * </pre>
     *
     * @param email email to look for
     * @return the user, or empty when no one has the email
     */
    Optional<User> findByEmailIgnoreCase(String email);
}
