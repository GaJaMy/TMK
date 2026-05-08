package com.tmk.api.user.exam.result;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamSourceType;
import java.time.OffsetDateTime;

public record ExamHistorySummaryResult(
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

    public static ExamHistorySummaryResult of(
            Exam exam,
            String title,
            int correctCount,
            int score,
            boolean pass
    ) {
        return new ExamHistorySummaryResult(
                exam.getId(),
                title,
                exam.getSourceType(),
                exam.getTotalQuestions(),
                exam.getTimeLimitMinutes(),
                exam.getSubmittedAt(),
                correctCount,
                score,
                pass
        );
    }
}
