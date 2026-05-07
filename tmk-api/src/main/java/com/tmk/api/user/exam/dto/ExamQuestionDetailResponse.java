package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamQuestionDetailResult;
import com.tmk.api.user.exam.result.ExamQuestionOptionResult;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;

public record ExamQuestionDetailResponse(
        Long examQuestionId,
        short orderNum,
        String content,
        QuestionType type,
        Difficulty difficulty,
        List<ExamQuestionOptionResponse> options,
        String myAnswer
) {

    public static ExamQuestionDetailResponse from(ExamQuestionDetailResult result) {
        List<ExamQuestionOptionResult> options = result.options();
        List<ExamQuestionOptionResponse> optionResponses = options.stream()
                .map(ExamQuestionOptionResponse::from)
                .toList();
        return new ExamQuestionDetailResponse(
                result.examQuestionId(),
                result.orderNum(),
                result.content(),
                result.type(),
                result.difficulty(),
                optionResponses,
                result.myAnswer()
        );
    }
}
