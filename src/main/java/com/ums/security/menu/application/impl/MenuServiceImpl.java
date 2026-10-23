package com.ums.security.menu.application.impl;

import com.ums.security.menu.application.MenuService;
import com.ums.security.menu.infrastructure.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
}
