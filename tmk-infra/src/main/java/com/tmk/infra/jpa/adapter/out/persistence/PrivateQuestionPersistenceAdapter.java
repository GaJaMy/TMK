package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.port.out.persistence.PrivateQuestionPort;
import com.tmk.core.question.entity.PrivateQuestion;
import com.tmk.infra.jpa.repository.PrivateQuestionJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrivateQuestionPersistenceAdapter implements PrivateQuestionPort {

    private final PrivateQuestionJpaRepository privateQuestionJpaRepository;

    @Override
    public PrivateQuestion save(PrivateQuestion privateQuestion) {
        return privateQuestionJpaRepository.save(privateQuestion);
    }

    @Override
    public List<PrivateQuestion> saveAll(List<PrivateQuestion> privateQuestions) {
        return privateQuestionJpaRepository.saveAll(privateQuestions);
    }

    @Override
    public Optional<PrivateQuestion> findById(Long privateQuestionId) {
        return privateQuestionJpaRepository.findById(privateQuestionId);
    }

    @Override
    public Optional<PrivateQuestion> findByIdAndUserId(Long privateQuestionId, Long userId) {
        return privateQuestionJpaRepository.findByIdAndUserId(privateQuestionId, userId);
    }

    @Override
    public List<PrivateQuestion> findAllByDocumentId(Long documentId) {
        return privateQuestionJpaRepository.findAllByDocumentIdOrderByIdAsc(documentId);
    }

    @Override
    public List<PrivateQuestion> findAllByDocumentIdLimit(Long documentId, int limit) {
        return privateQuestionJpaRepository.findAllByDocumentIdOrderByIdAsc(documentId, PageRequest.of(0, limit));
    }

    @Override
    public long countByDocumentId(Long documentId) {
        return privateQuestionJpaRepository.countByDocumentId(documentId);
    }

    @Override
    public void deleteAllByDocumentId(Long documentId) {
        privateQuestionJpaRepository.deleteAllByDocumentId(documentId);
    }
}
