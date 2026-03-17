package com.tmk.core.auth.service;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.port.out.EmailVerificationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class SendEmailVerificationServiceTest {

    @Mock
    private EmailVerificationPort emailVerificationPort;

    @InjectMocks
    private SendEmailVerificationService sendEmailVerificationService;

    @Test
    void sendVerification_newEmail_savesVerification() {
        // Arrange
        String email = "test@example.com";
        when(emailVerificationPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        sendEmailVerificationService.sendVerification(email);

        // Assert
        verify(emailVerificationPort).deleteByEmail(email);

        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationPort).save(captor.capture());

        EmailVerification saved = captor.getValue();
        assertThat(saved.getCode()).matches("\\d{6}");
    }

    @Test
    void sendVerification_existingEmail_deletesAndSaves() {
        // Arrange
        String email = "existing@example.com";
        when(emailVerificationPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        sendEmailVerificationService.sendVerification(email);

        // Assert: delete must be called before save
        var inOrder = inOrder(emailVerificationPort);
        inOrder.verify(emailVerificationPort).deleteByEmail(eq(email));
        inOrder.verify(emailVerificationPort).save(any(EmailVerification.class));
    }

    @Test
    void sendVerification_setsExpiryToFiveMinutes() {
        // Arrange
        String email = "test@example.com";
        OffsetDateTime before = OffsetDateTime.now();
        when(emailVerificationPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        sendEmailVerificationService.sendVerification(email);
        OffsetDateTime after = OffsetDateTime.now();

        // Assert
        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationPort).save(captor.capture());

        OffsetDateTime expiredAt = captor.getValue().getExpiredAt();
        assertThat(expiredAt).isAfter(before.plusMinutes(4).plusSeconds(59));
        assertThat(expiredAt).isBefore(after.plusMinutes(5).plusSeconds(1));
    }

    @Test
    void sendVerification_setsVerifiedFalse() {
        // Arrange
        String email = "test@example.com";
        when(emailVerificationPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        sendEmailVerificationService.sendVerification(email);

        // Assert
        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationPort).save(captor.capture());

        assertThat(captor.getValue().isVerified()).isFalse();
    }
}
