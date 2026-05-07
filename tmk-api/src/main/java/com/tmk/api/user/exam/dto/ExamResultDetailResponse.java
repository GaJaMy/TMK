package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamResultDetailResult;
import com.tmk.api.user.exam.result.ExamResultQuestionResult;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ExamResultDetailResponse(
        Long examId,
        String title,
        ExamSourceType sourceType,
        short totalQuestions,
        ExamStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime submittedAt,
        ExamResultSummaryResponse summary,
        List<ExamResultQuestionResponse> questions
) {

    public static ExamResultDetailResponse from(ExamResultDetailResult result) {
        List<ExamResultQuestionResult> questions = result.questions();
        List<ExamResultQuestionResponse> questionResponses = questions.stream()
                .map(ExamResultQuestionResponse::from)
                .toList();
        return new ExamResultDetailResponse(
                result.examId(),
                result.title(),
                result.sourceType(),
                result.totalQuestions(),
                result.status(),
                result.startedAt(),
                result.submittedAt(),
                ExamResultSummaryResponse.from(result.summary()),
                questionResponses
        );
    }
}
