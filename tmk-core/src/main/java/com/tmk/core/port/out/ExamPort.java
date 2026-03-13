package com.tmk.core.port.out;

import com.tmk.core.exam.entity.Exam;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamPort {

    Optional<Exam> findById(Long id);

    Optional<Exam> findByIdAndUserId(Long id, Long userId);

    List<Exam> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Exam> findExpiredInProgressExams(OffsetDateTime now);

    Exam save(Exam exam);
}
