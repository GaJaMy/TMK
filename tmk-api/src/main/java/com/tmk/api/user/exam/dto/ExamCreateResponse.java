package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamCreateResult;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;

public record ExamCreateResponse(
        Long examId,
        ExamSourceType sourceType,
        short totalQuestions,
        short timeLimit,
        ExamStatus status
) {

    public static ExamCreateResponse from(ExamCreateResult result) {
        return new ExamCreateResponse(
                result.examId(),
                result.sourceType(),
                result.totalQuestions(),
                result.timeLimitMinutes(),
                result.status()
        );
    }
}
