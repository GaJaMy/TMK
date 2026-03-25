package com.tmk.core.auth.service;

import com.tmk.core.port.out.RefreshTokenPort;
import com.tmk.core.port.out.TokenBlacklistPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenBlacklistPort tokenBlacklistPort;
    private final RefreshTokenPort refreshTokenPort;

    public void logout(Long userId, String accessToken, long remainingTtlSeconds) {
        if (remainingTtlSeconds > 0) {
            tokenBlacklistPort.blacklist(accessToken, remainingTtlSeconds);
        }
        refreshTokenPort.delete(userId);
    }
}
