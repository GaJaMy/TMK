package com.tmk.api.adapter.out.redis;

import com.tmk.core.port.out.cache.TokenBlacklistPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistAdapter implements TokenBlacklistPort {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {
        redisTemplate.opsForValue().set("token_blacklist:" + token, "true", ttlSeconds, TimeUnit.SECONDS);
    }
}
