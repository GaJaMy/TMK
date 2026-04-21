package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.emailverification.entity.EmailVerification;
import com.tmk.core.port.out.persistence.EmailVerificationPort;
import com.tmk.infra.jpa.repository.EmailVerificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationPersistenceAdapter implements EmailVerificationPort {

    private final EmailVerificationJpaRepository emailVerificationJpaRepository;

    @Override
    public Optional<EmailVerification> findByEmail(String email) {
        return emailVerificationJpaRepository.findByEmail(email);
    }

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return emailVerificationJpaRepository.save(emailVerification);
    }

    @Override
    public void deleteByEmail(String email) {
        emailVerificationJpaRepository.deleteByEmail(email);
    }

    @Override
    public void deleteExpiredBefore(OffsetDateTime threshold) {
        emailVerificationJpaRepository.deleteByExpiredAtBefore(threshold);
    }
}
