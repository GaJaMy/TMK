package com.tmk.core.port.out.cache;

import java.util.Optional;

public interface RefreshTokenPort {
    void save(Long userId, String token, long ttlSeconds);
    Optional<String> find(Long userId);
    void delete(Long userId);
}
