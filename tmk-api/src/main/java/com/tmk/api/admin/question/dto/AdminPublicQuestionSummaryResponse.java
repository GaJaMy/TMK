package com.tmk.api.admin.question.dto;

import com.tmk.api.admin.question.result.AdminPublicQuestionResult;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.time.OffsetDateTime;

public record AdminPublicQuestionSummaryResponse(
        Long questionId,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Long topicId,
        String topicName,
        boolean active,
        OffsetDateTime createdAt
) {

    public static AdminPublicQuestionSummaryResponse from(AdminPublicQuestionResult result) {
        return new AdminPublicQuestionSummaryResponse(
                result.questionId(),
                result.content(),
                result.type(),
                result.difficulty(),
                result.topicId(),
                result.topicName(),
                result.active(),
                result.createdAt()
        );
    }
}
