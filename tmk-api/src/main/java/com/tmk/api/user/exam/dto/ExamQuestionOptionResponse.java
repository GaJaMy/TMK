package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamQuestionOptionResult;

public record ExamQuestionOptionResponse(
        short optionNumber,
        String content
) {

    public static ExamQuestionOptionResponse from(ExamQuestionOptionResult result) {
        return new ExamQuestionOptionResponse(
                result.optionNumber(),
                result.content()
        );
    }
}
