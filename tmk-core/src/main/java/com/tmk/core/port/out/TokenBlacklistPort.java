package com.tmk.core.port.out;

public interface TokenBlacklistPort {
    void blacklist(String token, long ttlSeconds);
}
