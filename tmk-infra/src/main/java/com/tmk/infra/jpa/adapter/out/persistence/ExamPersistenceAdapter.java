package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.persistence.ExamPort;
import com.tmk.infra.jpa.repository.ExamJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExamPersistenceAdapter implements ExamPort {

    private final ExamJpaRepository examJpaRepository;

    @Override
    public Optional<Exam> findByIdAndUserId(Long id, Long userId) {
        return examJpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public List<Exam> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return examJpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Exam> findExpiredInProgressExams(OffsetDateTime now) {
        return examJpaRepository.findExpiredInProgressExams(now);
    }

    @Override
    public Exam save(Exam exam) {
        return examJpaRepository.save(exam);
    }
}
