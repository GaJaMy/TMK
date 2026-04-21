package com.tmk.api.adapter.out.redis;

import com.tmk.core.port.out.cache.RefreshTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenAdapter implements RefreshTokenPort {

    private final RedisTemplate<String, String> redisTemplate;

    private String key(Long userId) {
        return "refresh_token:" + userId;
    }

    @Override
    public void save(Long userId, String token, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(userId), token, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> find(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    @Override
    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }
}
