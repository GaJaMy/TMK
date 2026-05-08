package com.tmk.core.question.vo;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.time.OffsetDateTime;

public record PublicQuestionSearchResult(
        Long questionId,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Long topicId,
        String topicName,
        boolean active,
        OffsetDateTime createdAt
) {
}
