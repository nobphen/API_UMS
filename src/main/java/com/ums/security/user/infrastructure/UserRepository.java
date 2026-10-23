package com.ums.security.user.infrastructure;

import com.ums.security.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Login accepts either the username or the email. */
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
