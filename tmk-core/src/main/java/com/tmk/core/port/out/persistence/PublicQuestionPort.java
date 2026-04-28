package com.tmk.core.port.out.persistence;

import com.tmk.core.question.entity.PublicQuestion;
import java.util.List;
import java.util.Optional;

public interface PublicQuestionPort {

    PublicQuestion save(PublicQuestion publicQuestion);

    Optional<PublicQuestion> findById(Long publicQuestionId);

    List<PublicQuestion> findAll();

    List<PublicQuestion> findAllByTopicId(Long topicId);

    List<PublicQuestion> findAllActiveByTopicId(Long topicId);

    List<PublicQuestion> findAllActiveByTopicIdLimit(Long topicId, int limit);

    long countActiveByTopicId(Long topicId);

    void deleteById(Long publicQuestionId);
}
