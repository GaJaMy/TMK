package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamDetailResult;
import com.tmk.api.user.exam.result.ExamQuestionDetailResult;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ExamDetailResponse(
        Long examId,
        String title,
        ExamSourceType sourceType,
        short totalQuestions,
        short timeLimitMinutes,
        ExamStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime expiredAt,
        long remainingSeconds,
        List<ExamQuestionDetailResponse> questions
) {

    public static ExamDetailResponse from(ExamDetailResult result) {
        List<ExamQuestionDetailResult> questions = result.questions();
        List<ExamQuestionDetailResponse> questionResponses = questions.stream()
                .map(ExamQuestionDetailResponse::from)
                .toList();
        return new ExamDetailResponse(
                result.examId(),
                result.title(),
                result.sourceType(),
                result.totalQuestions(),
                result.timeLimitMinutes(),
                result.status(),
                result.startedAt(),
                result.expiredAt(),
                result.remainingSeconds(),
                questionResponses
        );
    }
}
