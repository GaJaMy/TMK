package com.tmk.api.user.exam.result;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ExamResultDetailResult(
        Long examId,
        String title,
        ExamSourceType sourceType,
        short totalQuestions,
        ExamStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        ExamResultSummaryResult summary,
        List<ExamResultQuestionResult> questions
) {

    public static ExamResultDetailResult of(
            Exam exam,
            String title,
            ExamResultSummaryResult summary,
            List<ExamResultQuestionResult> questions
    ) {
        return new ExamResultDetailResult(
                exam.getId(),
                title,
                exam.getSourceType(),
                exam.getTotalQuestions(),
                exam.getStatus(),
                exam.getStartedAt(),
                exam.getSubmittedAt(),
                summary,
                questions
        );
    }
}
