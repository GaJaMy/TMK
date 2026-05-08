package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamHistorySummaryResult;
import com.tmk.core.exam.entity.ExamSourceType;
import java.time.OffsetDateTime;

public record ExamHistorySummaryResponse(
        Long examId,
        String title,
        ExamSourceType sourceType,
        short totalQuestions,
        short timeLimitMinutes,
        OffsetDateTime submittedAt,
        int correctCount,
        int score,
        boolean pass
) {

    public static ExamHistorySummaryResponse from(ExamHistorySummaryResult result) {
        return new ExamHistorySummaryResponse(
                result.examId(),
                result.title(),
                result.sourceType(),
                result.totalQuestions(),
                result.timeLimitMinutes(),
                result.submittedAt(),
                result.correctCount(),
                result.score(),
                result.pass()
        );
    }
}
