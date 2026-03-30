package com.tmk.core.port.out;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface QuestionPort {

    Optional<Question> findById(Long id);

    List<Question> findAll();

    List<Question> findByTypeAndDifficulty(QuestionType type, Difficulty difficulty);

    List<Question> findByDocumentId(Long documentId);

    Question save(Question question);

    List<Question> findByFilters(QuestionType type, Difficulty difficulty, int offset, int limit);

    long countByFilters(QuestionType type, Difficulty difficulty);

    long countByDocumentId(Long documentId);

    // @MX:ANCHOR: [AUTO] Called by ExamCreationService; provides difficulty-grouped question map for exam creation
    // @MX:REASON: fan_in >= 1, critical business logic depends on this method
    default Map<Difficulty, List<Question>> findGroupedByDifficulty() {
        return findAll().stream().collect(Collectors.groupingBy(Question::getDifficulty));
    }
}
