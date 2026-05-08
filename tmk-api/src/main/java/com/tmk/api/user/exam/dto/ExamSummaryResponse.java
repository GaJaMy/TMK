package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamSummaryResult;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;

public record ExamSummaryResponse(
        Long examId,
        String title,
        ExamSourceType sourceType,
        short totalQuestions,
        short timeLimitMinutes,
        ExamStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime startedAt,
        OffsetDateTime expiredAt,
        long remainingSeconds
) {

    public static ExamSummaryResponse from(ExamSummaryResult result) {
        return new ExamSummaryResponse(
                result.examId(),
                result.title(),
                result.sourceType(),
                result.totalQuestions(),
                result.timeLimitMinutes(),
                result.status(),
                result.createdAt(),
                result.startedAt(),
                result.expiredAt(),
                result.remainingSeconds()
        );
    }
}
