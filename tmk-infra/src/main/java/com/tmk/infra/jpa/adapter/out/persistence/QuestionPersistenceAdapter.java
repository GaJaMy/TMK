package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.port.out.persistence.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.infra.jpa.repository.QuestionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuestionPersistenceAdapter implements QuestionPort {

    private final QuestionJpaRepository questionJpaRepository;

    @Override
    public Optional<Question> findById(Long id) {
        return questionJpaRepository.findById(id);
    }

    @Override
    public Optional<Question> findAccessibleById(Long id, Long ownerUserId) {
        return questionJpaRepository.findAccessibleById(id, ownerUserId);
    }

    @Override
    public List<Question> findAllByIds(List<Long> ids) {
        return questionJpaRepository.findAllById(ids);
    }

    @Override
    public List<Question> findAll() {
        return questionJpaRepository.findAll();
    }

    @Override
    public List<Question> findByTypeAndDifficulty(QuestionType type, Difficulty difficulty) {
        return questionJpaRepository.findByTypeAndDifficulty(type, difficulty);
    }

    @Override
    public List<Question> findByDocumentId(Long documentId) {
        return questionJpaRepository.findByDocumentId(documentId);
    }

    @Override
    public Question save(Question question) {
        return questionJpaRepository.save(question);
    }

    @Override
    public List<Question> findByFilters(QuestionType type, Difficulty difficulty, Topic topic, ContentScope scope, Long ownerUserId, int offset, int limit) {
        int page = (limit > 0) ? offset / limit : 0;
        return questionJpaRepository.findByOptionalFilters(type, difficulty, topic, scope, ownerUserId, PageRequest.of(page, limit)).getContent();
    }

    @Override
    public long countByFilters(QuestionType type, Difficulty difficulty, Topic topic, ContentScope scope, Long ownerUserId) {
        return questionJpaRepository.countByOptionalFilters(type, difficulty, topic, scope, ownerUserId);
    }

    @Override
    public List<Question> findCandidatesForExam(ContentScope scope, Long ownerUserId, Topic topic) {
        return questionJpaRepository.findCandidatesForExam(scope, ownerUserId, topic);
    }

    @Override
    public long countByDocumentId(Long documentId) {
        return questionJpaRepository.countByDocumentId(documentId);
    }

    @Override
    public Map<Difficulty, List<Question>> findGroupedByDifficulty(ContentScope scope, Long ownerUserId, Topic topic) {
        return questionJpaRepository.findCandidatesForExam(scope, ownerUserId, topic).stream()
                .collect(Collectors.groupingBy(Question::getDifficulty));
    }
}
