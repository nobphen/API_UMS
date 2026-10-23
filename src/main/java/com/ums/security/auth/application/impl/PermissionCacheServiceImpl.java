package com.ums.security.auth.application.impl;

import com.ums.security.auth.application.PermissionCacheService;
import com.ums.security.user.infrastructure.UserRoleRepository;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private static final String KEY = "auth:authorities:";
    private static final Duration TTL = Duration.ofHours(1);

    private final UserRoleRepository userRoleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Set<String> getAuthorities(Long userId) {
        Set<Object> cached = redisTemplate.opsForSet().members(KEY + userId);
        if (cached != null && !cached.isEmpty()) {
            return cached.stream().map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return reload(userId);
    }

    @Override
    public Set<String> reload(Long userId) {
        Set<String> authorities = new LinkedHashSet<>();
        userRoleRepository.findActiveRoleCodes(userId).forEach(code -> authorities.add("ROLE_" + code));
        authorities.addAll(userRoleRepository.findActivePermissionCodes(userId));

        String key = KEY + userId;
        redisTemplate.delete(key);
        if (!authorities.isEmpty()) {
            redisTemplate.opsForSet().add(key, authorities.toArray());
            redisTemplate.expire(key, TTL);
        }
        return authorities;
    }

    @Override
    public void evict(Long userId) {
        redisTemplate.delete(KEY + userId);
    }
}
