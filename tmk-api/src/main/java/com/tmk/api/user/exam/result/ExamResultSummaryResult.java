package com.tmk.api.user.exam.result;

public record ExamResultSummaryResult(
        int correctCount,
        int wrongCount,
        int score
) {
}
