package com.tmk.core.auth.service;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.EmailVerificationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyEmailServiceTest {

    @Mock
    private EmailVerificationPort emailVerificationPort;

    @InjectMocks
    private VerifyEmailService verifyEmailService;

    @Test
    void verify_validCode_setsVerifiedTrue() {
        // Arrange
        String email = "test@example.com";
        String code = "123456";
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(false)
                .expiredAt(OffsetDateTime.now().plusMinutes(5))
                .createdAt(OffsetDateTime.now())
                .build();
        when(emailVerificationPort.findByEmail(email)).thenReturn(Optional.of(verification));
        when(emailVerificationPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        verifyEmailService.verify(email, code);

        // Assert
        assertThat(verification.isVerified()).isTrue();
        verify(emailVerificationPort).save(verification);
    }

    @Test
    void verify_emailNotFound_throwsException() {
        // Arrange
        String email = "notfound@example.com";
        when(emailVerificationPort.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> verifyEmailService.verify(email, "123456"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_VERIFICATION_CODE));

        verify(emailVerificationPort, never()).save(any());
    }

    @Test
    void verify_expiredCode_throwsException() {
        // Arrange
        String email = "test@example.com";
        String code = "123456";
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(false)
                .expiredAt(OffsetDateTime.now().minusMinutes(1))
                .createdAt(OffsetDateTime.now().minusMinutes(6))
                .build();
        when(emailVerificationPort.findByEmail(email)).thenReturn(Optional.of(verification));

        // Act & Assert
        assertThatThrownBy(() -> verifyEmailService.verify(email, code))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_VERIFICATION_CODE));

        verify(emailVerificationPort, never()).save(any());
    }

    @Test
    void verify_wrongCode_throwsException() {
        // Arrange
        String email = "test@example.com";
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code("123456")
                .verified(false)
                .expiredAt(OffsetDateTime.now().plusMinutes(5))
                .createdAt(OffsetDateTime.now())
                .build();
        when(emailVerificationPort.findByEmail(email)).thenReturn(Optional.of(verification));

        // Act & Assert
        assertThatThrownBy(() -> verifyEmailService.verify(email, "999999"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_VERIFICATION_CODE));

        verify(emailVerificationPort, never()).save(any());
    }
}
