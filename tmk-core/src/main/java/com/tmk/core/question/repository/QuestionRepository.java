package com.tmk.core.question.repository;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTypeAndDifficulty(QuestionType type, Difficulty difficulty);

    List<Question> findByDocumentId(Long documentId);

    default Map<Difficulty, List<Question>> findGroupedByDifficulty() {
        return findAll().stream().collect(Collectors.groupingBy(Question::getDifficulty));
    }
}
