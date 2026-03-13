package com.tmk.core.emailverification.repository;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.port.out.EmailVerificationPort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long>, EmailVerificationPort {

    Optional<EmailVerification> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiredAtBefore(OffsetDateTime threshold);
}
