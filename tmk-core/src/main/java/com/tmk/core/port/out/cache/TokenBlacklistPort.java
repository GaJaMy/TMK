package com.tmk.core.port.out.cache;

import java.time.Duration;

public interface TokenBlacklistPort {

    void blacklist(String accessToken, Duration ttl);

    boolean isBlacklisted(String accessToken);
}
