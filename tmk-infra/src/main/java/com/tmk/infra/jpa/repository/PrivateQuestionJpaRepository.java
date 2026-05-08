package com.tmk.infra.jpa.repository;

import com.tmk.core.question.entity.PrivateQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateQuestionJpaRepository extends JpaRepository<PrivateQuestion, Long> {

    @EntityGraph(attributePaths = "options")
    Optional<PrivateQuestion> findById(Long id);

    @EntityGraph(attributePaths = "options")
    Optional<PrivateQuestion> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = "options")
    List<PrivateQuestion> findAllByDocumentIdOrderByIdAsc(Long documentId);

    @EntityGraph(attributePaths = "options")
    List<PrivateQuestion> findAllByDocumentIdOrderByIdAsc(Long documentId, Pageable pageable);

    long countByDocumentId(Long documentId);

    void deleteAllByDocumentId(Long documentId);
}
