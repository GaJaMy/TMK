package com.tmk.api.admin.question.result;

import com.tmk.api.question.support.QuestionAnswerSupport;
import com.tmk.api.question.support.QuestionAnswerSupport.QuestionOptionCandidate;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.QuestionType;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminPublicQuestionDetailResult(
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

    public static AdminPublicQuestionDetailResult from(PublicQuestion question, String topicName) {
        var questionOptions = question.getOptions();
        List<String> options = questionOptions.stream()
                .sorted(java.util.Comparator.comparingInt(option -> option.getOptionNumber()))
                .map(option -> option.getContent())
                .toList();
        List<QuestionOptionCandidate> candidates = questionOptions.stream()
                .map(option -> new QuestionOptionCandidate(option.getOptionNumber(), option.getContent()))
                .toList();

        return new AdminPublicQuestionDetailResult(
                question.getId(),
                question.getContent(),
                question.getType(),
                question.getDifficulty(),
                question.getTopicId(),
                topicName,
                QuestionAnswerSupport.toDisplayAnswer(question.getType(), question.getAnswer(), candidates),
                question.getExplanation(),
                options,
                question.isActive(),
                question.getCreatedAt()
        );
    }
}
