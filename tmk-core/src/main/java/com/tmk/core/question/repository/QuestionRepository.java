package com.tmk.core.question.repository;

import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface QuestionRepository extends JpaRepository<Question, Long>, QuestionPort {

    List<Question> findByTypeAndDifficulty(QuestionType type, Difficulty difficulty);

    List<Question> findByDocumentId(Long documentId);

    long countByDocumentId(Long documentId);

    @Query("SELECT q FROM Question q WHERE (:type IS NULL OR q.type = :type) AND (:difficulty IS NULL OR q.difficulty = :difficulty) ORDER BY q.id DESC")
    Page<Question> findByOptionalFilters(
        @Param("type") QuestionType type,
        @Param("difficulty") Difficulty difficulty,
        Pageable pageable
    );

    @Query("SELECT COUNT(q) FROM Question q WHERE (:type IS NULL OR q.type = :type) AND (:difficulty IS NULL OR q.difficulty = :difficulty)")
    long countByOptionalFilters(
        @Param("type") QuestionType type,
        @Param("difficulty") Difficulty difficulty
    );

    @Override
    default List<Question> findByFilters(QuestionType type, Difficulty difficulty, int offset, int limit) {
        int page = (limit > 0) ? offset / limit : 0;
        return findByOptionalFilters(type, difficulty, PageRequest.of(page, limit)).getContent();
    }

    @Override
    default long countByFilters(QuestionType type, Difficulty difficulty) {
        return countByOptionalFilters(type, difficulty);
    }

    @Override
    default Map<Difficulty, List<Question>> findGroupedByDifficulty() {
        return findAll().stream().collect(Collectors.groupingBy(Question::getDifficulty));
    }
}
