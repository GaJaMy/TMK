package com.tmk.api.adapter.out.redis;

import com.tmk.core.port.out.cache.TokenBlacklistPort;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistAdapter implements TokenBlacklistPort {

    private final RedisTemplate<String, String> redisTemplate;

    private String key(String token) {
        return "token_blacklist:" + token;
    }

    @Override
    public void blacklist(String token, Duration ttl) {
        redisTemplate.opsForValue().set(key(token), "true", ttl.toSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
    }
}
