package com.tmk.api.user.exam.request;

import com.tmk.core.exam.entity.ExamSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExamCreateRequest(
        @NotNull ExamSourceType sourceType,
        Long topicId,
        Long documentId,
        @Min(1) short questionCount,
        @Min(1) short timeLimitMinutes
) {
}
