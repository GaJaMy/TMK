package com.tmk.core.auth.service;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.EmailVerificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class VerifyEmailService {

    private final EmailVerificationPort emailVerificationPort;

    public void verify(String email, String code) {
        EmailVerification verification = emailVerificationPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));

        if (!verification.getCode().equals(code)) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        if (OffsetDateTime.now().isAfter(verification.getExpiredAt())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        verification.verify();
        emailVerificationPort.save(verification);
    }
}
