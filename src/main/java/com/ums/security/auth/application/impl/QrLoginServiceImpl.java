package com.ums.security.auth.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.security.auth.api.dto.QrLoginResponse;
import com.ums.security.auth.application.QrLoginService;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QrLoginServiceImpl implements QrLoginService {

    private static final String KEY = "auth:qr:";
    private static final Duration TTL = Duration.ofMinutes(2);
    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public QrLoginResponse create() {
        String qrId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY + qrId, PENDING, TTL);
        return new QrLoginResponse(qrId, TTL.toSeconds());
    }

    @Override
    public void approve(String qrId, Long userId) {
        String key = KEY + qrId;
        if (!PENDING.equals(redisTemplate.opsForValue().get(key))) {
            throw new BusinessException("QR code has expired or was already used");
        }
        Long secondsLeft = redisTemplate.getExpire(key);
        Duration ttl = secondsLeft == null || secondsLeft <= 0 ? TTL : Duration.ofSeconds(secondsLeft);
        redisTemplate.opsForValue().set(key, APPROVED + userId, ttl);
    }

    @Override
    public Optional<Long> consumeApproved(String qrId) {
        String key = KEY + qrId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new BusinessException("QR code has expired");
        }
        String state = value.toString();
        if (!state.startsWith(APPROVED)) {
            return Optional.empty();
        }
        // delete() returns true only for the first caller, so one approval gives exactly one login
        if (!Boolean.TRUE.equals(redisTemplate.delete(key))) {
            throw new BusinessException("QR code was already used");
        }
        return Optional.of(Long.valueOf(state.substring(APPROVED.length())));
    }
}
