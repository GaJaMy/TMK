package com.tmk.core.port.out.persistence;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface QuestionPort {

    Optional<Question> findById(Long id);

    Optional<Question> findAccessibleById(Long id, Long ownerUserId);

    List<Question> findAllByIds(List<Long> ids);

    List<Question> findAll();

    List<Question> findByTypeAndDifficulty(QuestionType type, Difficulty difficulty);

    List<Question> findByDocumentId(Long documentId);

    Question save(Question question);

    List<Question> findByFilters(QuestionType type, Difficulty difficulty, Topic topic, ContentScope scope, Long ownerUserId, int offset, int limit);

    long countByFilters(QuestionType type, Difficulty difficulty, Topic topic, ContentScope scope, Long ownerUserId);

    List<Question> findCandidatesForExam(ContentScope scope, Long ownerUserId, Topic topic);

    long countByDocumentId(Long documentId);

    default Map<Difficulty, List<Question>> findGroupedByDifficulty(ContentScope scope, Long ownerUserId, Topic topic) {
        return findCandidatesForExam(scope, ownerUserId, topic).stream()
                .collect(Collectors.groupingBy(Question::getDifficulty));
    }
}
