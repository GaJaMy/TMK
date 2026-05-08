package com.tmk.api.user.exam.result;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamSourceType;
import com.tmk.core.exam.entity.ExamStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record ExamDetailResult(
        Long examId,
        String title,
        ExamSourceType sourceType,
        short totalQuestions,
        short timeLimitMinutes,
        ExamStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime expiredAt,
        long remainingSeconds,
        List<ExamQuestionDetailResult> questions
) {

    public static ExamDetailResult of(
            Exam exam,
            String title,
            OffsetDateTime now,
            List<ExamQuestionDetailResult> questions
    ) {
        return new ExamDetailResult(
                exam.getId(),
                title,
                exam.getSourceType(),
                exam.getTotalQuestions(),
                exam.getTimeLimitMinutes(),
                exam.getStatus(),
                exam.getStartedAt(),
                exam.getExpiredAt(),
                exam.getRemainingSeconds(now),
                questions
        );
    }
}
