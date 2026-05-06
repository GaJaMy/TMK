package com.tmk.api.user.exam.result;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;

public record ExamSummaryResult(
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

    public static ExamSummaryResult of(Exam exam, String title, OffsetDateTime now) {
        return new ExamSummaryResult(
                exam.getId(),
                title,
                exam.getSourceType(),
                exam.getTotalQuestions(),
                exam.getTimeLimitMinutes(),
                exam.getStatus(),
                exam.getCreatedAt(),
                exam.getStartedAt(),
                exam.getExpiredAt(),
                exam.getStatus() == ExamStatus.IN_PROGRESS ? exam.getRemainingSeconds(now) : 0L
        );
    }
}
