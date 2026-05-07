package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamResultSummaryResult;

public record ExamResultSummaryResponse(
        int correctCount,
        int wrongCount,
        int score
) {

    public static ExamResultSummaryResponse from(ExamResultSummaryResult result) {
        return new ExamResultSummaryResponse(
                result.correctCount(),
                result.wrongCount(),
                result.score()
        );
    }
}
