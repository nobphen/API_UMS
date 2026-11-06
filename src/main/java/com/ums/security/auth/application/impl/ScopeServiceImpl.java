package com.ums.security.auth.application.impl;

import com.ums.security.auth.application.ScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Not written yet. Later: checks scoped roles (user_roles.scope_type + scope_id), see ScopeService. */
@Service
@Transactional
@RequiredArgsConstructor
public class ScopeServiceImpl implements ScopeService {
}
