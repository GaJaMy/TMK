package com.tmk.core.port.out.persistence;

import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PublicQuestionSearchResult;
import java.util.List;
import java.util.Optional;

public interface PublicQuestionPort {

    PublicQuestion save(PublicQuestion publicQuestion);

    Optional<PublicQuestion> findById(Long publicQuestionId);

    List<PublicQuestion> findAll();

    List<PublicQuestionSearchResult> search(
            Long topicId,
            Difficulty difficulty,
            QuestionType type,
            Boolean active
    );

    List<PublicQuestion> findAllByTopicId(Long topicId);

    List<PublicQuestion> findAllActiveByTopicId(Long topicId);

    List<PublicQuestion> findAllActiveByTopicIdLimit(Long topicId, int limit);

    long countActiveByTopicId(Long topicId);

    void deleteById(Long publicQuestionId);
}
