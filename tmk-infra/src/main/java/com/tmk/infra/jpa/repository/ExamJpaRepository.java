package com.tmk.infra.jpa.repository;

import com.tmk.core.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ExamJpaRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByIdAndUserId(Long id, Long userId);

    List<Exam> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT e FROM Exam e WHERE e.status = 'IN_PROGRESS' AND e.expiredAt < :now")
    List<Exam> findExpiredInProgressExams(@Param("now") OffsetDateTime now);
}
