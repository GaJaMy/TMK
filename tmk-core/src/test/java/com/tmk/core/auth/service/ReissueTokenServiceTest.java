package com.tmk.core.auth.service;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.cache.RefreshTokenPort;
import com.tmk.core.port.out.persistence.UserPort;
import com.tmk.core.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReissueTokenServiceTest {

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private UserPort userPort;

    @InjectMocks
    private ReissueTokenService reissueTokenService;

    @Test
    void validateRefreshToken_validToken_returnsUser() {
        // Arrange
        Long userId = 1L;
        String refreshToken = "valid.refresh.token";
        User user = User.create("test@example.com", "encodedPassword");

        when(refreshTokenPort.find(userId)).thenReturn(Optional.of(refreshToken));
        when(userPort.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User result = reissueTokenService.validateRefreshTokenAndGetUser(userId, refreshToken);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void validateRefreshToken_noStoredToken_throwsException() {
        // Arrange
        Long userId = 1L;
        when(refreshTokenPort.find(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reissueTokenService.validateRefreshTokenAndGetUser(userId, "any.token"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    void validateRefreshToken_tokenMismatch_throwsException() {
        // Arrange
        Long userId = 1L;
        String storedToken = "stored.refresh.token";
        String providedToken = "different.refresh.token";

        when(refreshTokenPort.find(userId)).thenReturn(Optional.of(storedToken));

        // Act & Assert
        assertThatThrownBy(() -> reissueTokenService.validateRefreshTokenAndGetUser(userId, providedToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_INVALID);
    }

    @Test
    void validateRefreshToken_userNotFound_throwsException() {
        // Arrange
        Long userId = 1L;
        String refreshToken = "valid.refresh.token";

        when(refreshTokenPort.find(userId)).thenReturn(Optional.of(refreshToken));
        when(userPort.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reissueTokenService.validateRefreshTokenAndGetUser(userId, refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_INVALID);
    }
}
