package com.ums.security.menu.infrastructure;

import com.ums.security.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for the menus table. Only the built-in JpaRepository methods for now. */
public interface MenuRepository extends JpaRepository<Menu, Long> {
}
