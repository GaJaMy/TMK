package com.tmk.core.auth.service;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.EmailVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class VerifyEmailService {

    private final EmailVerificationPort emailVerificationPort;

    @Transactional
    public void verify(String email, String code) {
        EmailVerification verification = emailVerificationPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));

        if (verification.getExpiredAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        if (!code.equals(verification.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        verification.verify();
        emailVerificationPort.save(verification);
    }
}
