package com.tmk.core.auth.service;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.PasswordEncoderPort;
import com.tmk.core.port.out.UserPort;
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
class LoginServiceTest {

    @Mock
    private UserPort userPort;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    void login_validCredentials_returnsUser() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "password123";
        User user = User.create(email, "encodedPassword");

        when(userPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(true);

        // Act
        User result = loginService.login(email, rawPassword);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void login_emailNotFound_throwsInvalidCredentials() {
        // Arrange
        String email = "unknown@example.com";
        when(userPort.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(email, "anyPassword"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "wrongPassword";
        User user = User.create(email, "encodedPassword");

        when(userPort.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(email, rawPassword))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }
}
