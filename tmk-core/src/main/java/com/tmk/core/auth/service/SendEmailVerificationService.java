package com.tmk.core.auth.service;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.port.out.EmailVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SendEmailVerificationService {

    private final EmailVerificationPort emailVerificationPort;

    @Transactional
    public void sendVerification(String email) {
        emailVerificationPort.deleteByEmail(email);

        String code = generateCode();
        OffsetDateTime now = OffsetDateTime.now();

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(false)
                .expiredAt(now.plusMinutes(5))
                .createdAt(now)
                .build();

        emailVerificationPort.save(verification);
        // TODO: SMTP 연동 시 여기서 이메일 발송
    }

    private String generateCode() {
        return String.format("%06d", new java.util.Random().nextInt(1_000_000));
    }
}
