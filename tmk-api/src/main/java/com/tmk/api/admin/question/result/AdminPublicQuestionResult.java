package com.tmk.api.admin.question.result;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PublicQuestionSearchResult;
import java.time.OffsetDateTime;

public record AdminPublicQuestionResult(
        Long questionId,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Long topicId,
        String topicName,
        boolean active,
        OffsetDateTime createdAt
) {

    public static AdminPublicQuestionResult from(PublicQuestionSearchResult question) {
        return new AdminPublicQuestionResult(
                question.questionId(),
                question.content(),
                question.type(),
                question.difficulty(),
                question.topicId(),
                question.topicName(),
                question.active(),
                question.createdAt()
        );
    }
}
