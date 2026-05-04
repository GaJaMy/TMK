package com.tmk.infra.jpa.adapter.out.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.QPublicQuestion;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PublicQuestionSearchResult;
import com.tmk.core.topic.entity.QTopic;
import com.tmk.infra.jpa.repository.PublicQuestionJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicQuestionPersistenceAdapter implements PublicQuestionPort {

    private static final QPublicQuestion publicQuestion = QPublicQuestion.publicQuestion;
    private static final QTopic topic = QTopic.topic;

    private final PublicQuestionJpaRepository publicQuestionJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

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
    public List<PublicQuestionSearchResult> search(
            Long topicId,
            Difficulty difficulty,
            QuestionType type,
            Boolean active
    ) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        PublicQuestionSearchResult.class,
                        publicQuestion.id,
                        publicQuestion.content,
                        publicQuestion.type,
                        publicQuestion.difficulty,
                        publicQuestion.topicId,
                        topic.name,
                        publicQuestion.active,
                        publicQuestion.createdAt
                ))
                .from(publicQuestion)
                .join(topic).on(publicQuestion.topicId.eq(topic.id))
                .where(
                        topicIdEq(topicId),
                        difficultyEq(difficulty),
                        typeEq(type),
                        activeEq(active)
                )
                .orderBy(publicQuestion.createdAt.desc())
                .fetch();
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

    private BooleanExpression topicIdEq(Long topicId) {
        return topicId == null ? null : publicQuestion.topicId.eq(topicId);
    }

    private BooleanExpression difficultyEq(Difficulty difficulty) {
        return difficulty == null ? null : publicQuestion.difficulty.eq(difficulty);
    }

    private BooleanExpression typeEq(QuestionType type) {
        return type == null ? null : publicQuestion.type.eq(type);
    }

    private BooleanExpression activeEq(Boolean active) {
        return active == null ? null : publicQuestion.active.eq(active);
    }
}
