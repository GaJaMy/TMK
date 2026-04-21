package com.tmk.core.port.out.persistence;

import com.tmk.core.emailverification.entity.EmailVerification;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EmailVerificationPort {

    Optional<EmailVerification> findByEmail(String email);

    EmailVerification save(EmailVerification emailVerification);

    void deleteByEmail(String email);

    void deleteExpiredBefore(OffsetDateTime threshold);
}
