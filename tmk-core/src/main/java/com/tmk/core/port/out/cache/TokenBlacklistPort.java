package com.tmk.core.port.out.cache;

public interface TokenBlacklistPort {
    void blacklist(String token, long ttlSeconds);
}
