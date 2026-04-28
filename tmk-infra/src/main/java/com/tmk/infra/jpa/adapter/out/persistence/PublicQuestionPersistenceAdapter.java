package com.tmk.infra.jpa.adapter.out.persistence;

import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.infra.jpa.repository.PublicQuestionJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicQuestionPersistenceAdapter implements PublicQuestionPort {

    private final PublicQuestionJpaRepository publicQuestionJpaRepository;

    @Override
    public PublicQuestion save(PublicQuestion publicQuestion) {
        return publicQuestionJpaRepository.save(publicQuestion);
    }

    @Override
    public Optional<PublicQuestion> findById(Long publicQuestionId) {
        return publicQuestionJpaRepository.findById(publicQuestionId);
    }

    @Override
    public List<PublicQuestion> findAll() {
        return publicQuestionJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<PublicQuestion> findAllByTopicId(Long topicId) {
        return publicQuestionJpaRepository.findAllByTopicIdOrderByCreatedAtDesc(topicId);
    }

    @Override
    public List<PublicQuestion> findAllActiveByTopicId(Long topicId) {
        return publicQuestionJpaRepository.findAllByTopicIdAndActiveTrueOrderByCreatedAtDesc(topicId);
    }

    @Override
    public List<PublicQuestion> findAllActiveByTopicIdLimit(Long topicId, int limit) {
        return publicQuestionJpaRepository.findAllByTopicIdAndActiveTrueOrderByCreatedAtDesc(
                topicId,
                PageRequest.of(0, limit)
        );
    }

    @Override
    public long countActiveByTopicId(Long topicId) {
        return publicQuestionJpaRepository.countByTopicIdAndActiveTrue(topicId);
    }

    @Override
    public void deleteById(Long publicQuestionId) {
        publicQuestionJpaRepository.deleteById(publicQuestionId);
    }
}
