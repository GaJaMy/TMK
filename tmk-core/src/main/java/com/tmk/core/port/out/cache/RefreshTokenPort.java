package com.tmk.core.port.out.cache;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenPort {

    void save(String principalType, Long principalId, String refreshToken, Duration ttl);

    Optional<String> find(String principalType, Long principalId);

    void delete(String principalType, Long principalId);
}
