package com.tmk.infra.jpa.repository;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {

    List<Question> findByTypeAndDifficulty(QuestionType type, Difficulty difficulty);

    List<Question> findByDocumentId(Long documentId);

    long countByDocumentId(Long documentId);

    @Query("""
            SELECT q FROM Question q
            WHERE q.scope = :scope
              AND (:ownerUserId IS NULL OR q.ownerUserId = :ownerUserId)
              AND (:type IS NULL OR q.type = :type)
              AND (:difficulty IS NULL OR q.difficulty = :difficulty)
              AND (:topic IS NULL OR q.topic = :topic)
            ORDER BY q.id DESC
            """)
    Page<Question> findByOptionalFilters(
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("topic") Topic topic,
            @Param("scope") ContentScope scope,
            @Param("ownerUserId") Long ownerUserId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(q) FROM Question q
            WHERE q.scope = :scope
              AND (:ownerUserId IS NULL OR q.ownerUserId = :ownerUserId)
              AND (:type IS NULL OR q.type = :type)
              AND (:difficulty IS NULL OR q.difficulty = :difficulty)
              AND (:topic IS NULL OR q.topic = :topic)
            """)
    long countByOptionalFilters(
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("topic") Topic topic,
            @Param("scope") ContentScope scope,
            @Param("ownerUserId") Long ownerUserId
    );

    @Query("""
            SELECT q FROM Question q
            WHERE q.id = :id
              AND (q.scope = com.tmk.core.common.ContentScope.PUBLIC
                   OR (q.scope = com.tmk.core.common.ContentScope.PRIVATE AND q.ownerUserId = :ownerUserId))
            """)
    Optional<Question> findAccessibleById(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);

    @Query("""
            SELECT q FROM Question q
            WHERE q.scope = :scope
              AND (:ownerUserId IS NULL OR q.ownerUserId = :ownerUserId)
              AND (:topic IS NULL OR q.topic = :topic)
            """)
    List<Question> findCandidatesForExam(
            @Param("scope") ContentScope scope,
            @Param("ownerUserId") Long ownerUserId,
            @Param("topic") Topic topic
    );
}
