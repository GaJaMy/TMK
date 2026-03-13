package com.tmk.core.auth.service;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.port.out.EmailVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SendEmailVerificationService {

    private final EmailVerificationPort emailVerificationPort;

    public void sendVerification(String email) {
        // Delete existing verification code (upsert pattern)
        emailVerificationPort.findByEmail(email).ifPresent(ev ->
                emailVerificationPort.deleteByEmail(email));

        // Generate 6-digit random code
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(false)
                .expiredAt(OffsetDateTime.now().plusMinutes(5))
                .createdAt(OffsetDateTime.now())
                .build();

        emailVerificationPort.save(verification);
        // Note: actual email sending is handled at the infrastructure layer (MVP only saves the code)
    }
}
