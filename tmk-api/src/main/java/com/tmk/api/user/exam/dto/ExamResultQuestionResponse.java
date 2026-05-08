package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamQuestionOptionResult;
import com.tmk.api.user.exam.result.ExamResultQuestionResult;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;

public record ExamResultQuestionResponse(
        Long examQuestionId,
        short orderNum,
        String content,
        QuestionType type,
        Difficulty difficulty,
        List<ExamQuestionOptionResponse> options,
        String myAnswer,
        String correctAnswer,
        boolean correct,
        String explanation
) {

    public static ExamResultQuestionResponse from(ExamResultQuestionResult result) {
        List<ExamQuestionOptionResult> options = result.options();
        List<ExamQuestionOptionResponse> optionResponses = options.stream()
                .map(ExamQuestionOptionResponse::from)
                .toList();
        return new ExamResultQuestionResponse(
                result.examQuestionId(),
                result.orderNum(),
                result.content(),
                result.type(),
                result.difficulty(),
                optionResponses,
                result.myAnswer(),
                result.correctAnswer(),
                result.correct(),
                result.explanation()
        );
    }
}
