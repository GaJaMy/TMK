package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.persistence.ExamPort;
import com.tmk.infra.jpa.repository.ExamJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamPersistenceAdapter implements ExamPort {

    private final ExamJpaRepository examJpaRepository;

    @Override
    public Exam save(Exam exam) {
        return examJpaRepository.save(exam);
    }

    @Override
    public Optional<Exam> findById(Long examId) {
        return examJpaRepository.findById(examId);
    }

    @Override
    public Optional<Exam> findByIdAndUserId(Long examId, Long userId) {
        return examJpaRepository.findByIdAndUserId(examId, userId);
    }

    @Override
    public Optional<Exam> findInProgressByUserId(Long userId) {
        return examJpaRepository.findByUserIdAndStatus(userId, com.tmk.core.exam.entity.ExamStatus.IN_PROGRESS);
    }

    @Override
    public List<Exam> findHistoryByUserIdOrderByCreatedAtDesc(Long userId) {
        return examJpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Exam> findExpiredInProgressExams(OffsetDateTime now) {
        return examJpaRepository.findExpiredInProgressExams(now);
    }
}
