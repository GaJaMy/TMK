package com.tmk.api.user.exam.result;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;

public record ExamStartResult(
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

    public static ExamStartResult of(Exam exam, String title, OffsetDateTime now) {
        return new ExamStartResult(
                exam.getId(),
                title,
                exam.getSourceType(),
                exam.getTotalQuestions(),
                exam.getTimeLimitMinutes(),
                exam.getStartedAt(),
                exam.getExpiredAt(),
                exam.getStatus(),
                exam.getRemainingSeconds(now)
        );
    }
}
