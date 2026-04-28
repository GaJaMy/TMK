package com.tmk.infra.jpa.repository;

import com.tmk.core.question.entity.PublicQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicQuestionJpaRepository extends JpaRepository<PublicQuestion, Long> {

    @EntityGraph(attributePaths = "options")
    Optional<PublicQuestion> findById(Long id);

    @EntityGraph(attributePaths = "options")
    List<PublicQuestion> findAllByTopicIdOrderByCreatedAtDesc(Long topicId);

    @EntityGraph(attributePaths = "options")
    List<PublicQuestion> findAllByTopicIdAndActiveTrueOrderByCreatedAtDesc(Long topicId);

    @EntityGraph(attributePaths = "options")
    List<PublicQuestion> findAllByTopicIdAndActiveTrueOrderByCreatedAtDesc(Long topicId, Pageable pageable);

    List<PublicQuestion> findAllByOrderByCreatedAtDesc();

    long countByTopicIdAndActiveTrue(Long topicId);

    boolean existsByTopicId(Long topicId);
}
