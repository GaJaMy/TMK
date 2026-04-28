package com.tmk.api.adapter.out.redis;

import com.tmk.core.port.out.cache.RefreshTokenPort;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements RefreshTokenPort {

    private final RedisTemplate<String, String> redisTemplate;

    private String key(String principalType, Long principalId) {
        return "refresh_token:" + principalType + ":" + principalId;
    }

    @Override
    public void save(String principalType, Long principalId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(
                key(principalType, principalId),
                refreshToken,
                ttl.toSeconds(),
                TimeUnit.SECONDS
        );
    }

    @Override
    public Optional<String> find(String principalType, Long principalId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(principalType, principalId)));
    }

    @Override
    public void delete(String principalType, Long principalId) {
        redisTemplate.delete(key(principalType, principalId));
    }
}
