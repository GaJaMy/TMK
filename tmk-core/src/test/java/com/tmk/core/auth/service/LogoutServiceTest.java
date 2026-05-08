package com.tmk.core.auth.service;

import com.tmk.core.port.out.cache.RefreshTokenPort;
import com.tmk.core.port.out.cache.TokenBlacklistPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void logout_withPositiveTtl_blacklistsTokenAndDeletesRefreshToken() {
        // Arrange
        Long userId = 1L;
        String accessToken = "access.token.value";
        long remainingTtl = 3600L;

        // Act
        logoutService.logout(userId, accessToken, remainingTtl);

        // Assert
        verify(tokenBlacklistPort).blacklist(accessToken, remainingTtl);
        verify(refreshTokenPort).delete(userId);
    }

    @Test
    void logout_withZeroTtl_onlyDeletesRefreshToken() {
        // Arrange
        Long userId = 1L;
        String accessToken = "access.token.value";
        long remainingTtl = 0L;

        // Act
        logoutService.logout(userId, accessToken, remainingTtl);

        // Assert
        verify(tokenBlacklistPort, never()).blacklist(accessToken, remainingTtl);
        verify(refreshTokenPort).delete(userId);
    }

    @Test
    void logout_withNegativeTtl_onlyDeletesRefreshToken() {
        // Arrange
        Long userId = 1L;
        String accessToken = "access.token.value";
        long remainingTtl = -100L;

        // Act
        logoutService.logout(userId, accessToken, remainingTtl);

        // Assert
        verify(tokenBlacklistPort, never()).blacklist(accessToken, remainingTtl);
        verify(refreshTokenPort).delete(userId);
    }
}
