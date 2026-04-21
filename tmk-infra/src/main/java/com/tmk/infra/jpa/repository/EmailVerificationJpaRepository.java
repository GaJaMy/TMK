package com.tmk.infra.jpa.repository;

import com.tmk.core.emailverification.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiredAtBefore(OffsetDateTime threshold);
}
