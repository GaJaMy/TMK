package com.tmk.api.user.exam.dto;

import com.tmk.api.user.exam.result.ExamStartResult;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;

public record ExamStartResponse(
        Long examId,
        String title,
        ExamSourceType sourceType,
        short totalQuestions,
        short timeLimitMinutes,
        OffsetDateTime startedAt,
        OffsetDateTime expiredAt,
        ExamStatus status,
        long remainingSeconds
) {

    public static ExamStartResponse from(ExamStartResult result) {
        return new ExamStartResponse(
                result.examId(),
                result.title(),
                result.sourceType(),
                result.totalQuestions(),
                result.timeLimitMinutes(),
                result.startedAt(),
                result.expiredAt(),
                result.status(),
                result.remainingSeconds()
        );
    }
}
