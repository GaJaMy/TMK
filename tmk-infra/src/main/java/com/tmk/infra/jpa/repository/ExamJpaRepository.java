package com.tmk.infra.jpa.repository;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamJpaRepository extends JpaRepository<Exam, Long> {

    @EntityGraph(attributePaths = "examQuestions")
    Optional<Exam> findByIdAndUserId(Long examId, Long userId);

    @EntityGraph(attributePaths = "examQuestions")
    Optional<Exam> findByUserIdAndStatus(Long userId, ExamStatus status);

    @EntityGraph(attributePaths = "examQuestions")
    List<Exam> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "examQuestions")
    @Query("SELECT e FROM Exam e WHERE e.status = 'IN_PROGRESS' AND e.expiredAt < :now")
    List<Exam> findExpiredInProgressExams(@Param("now") OffsetDateTime now);
}
