package com.ums.security.menu.application.impl;

import com.ums.security.menu.application.MenuService;
import com.ums.security.menu.infrastructure.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Not written yet. Later: implements MenuService (menu CRUD and the menu for a user's permissions). */
@Service
@Transactional
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
}
