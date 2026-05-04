package com.tmk.api.admin.question.dto;

import com.tmk.api.admin.question.result.AdminPublicQuestionDetailResult;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminPublicQuestionDetailResponse(
        Long questionId,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Long topicId,
        String topicName,
        String answer,
        String explanation,
        List<String> options,
        boolean active,
        OffsetDateTime createdAt
) {

    public static AdminPublicQuestionDetailResponse from(AdminPublicQuestionDetailResult result) {
        return new AdminPublicQuestionDetailResponse(
                result.questionId(),
                result.content(),
                result.type(),
                result.difficulty(),
                result.topicId(),
                result.topicName(),
                result.answer(),
                result.explanation(),
                result.options(),
                result.active(),
                result.createdAt()
        );
    }
}
