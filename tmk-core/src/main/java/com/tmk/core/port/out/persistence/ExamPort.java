package com.tmk.core.port.out.persistence;

import com.tmk.core.exam.entity.Exam;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamPort {

    Exam save(Exam exam);

    Optional<Exam> findById(Long examId);

    Optional<Exam> findByIdAndUserId(Long examId, Long userId);

    Optional<Exam> findInProgressByUserId(Long userId);

    List<Exam> findHistoryByUserIdOrderByCreatedAtDesc(Long userId);

    List<Exam> findExpiredInProgressExams(OffsetDateTime now);
}
