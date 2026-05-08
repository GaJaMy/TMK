package com.tmk.api.admin.question.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminPublicQuestionBulkDeleteRequest(
        @NotEmpty(message = "공용 문제 ID 목록은 비어 있을 수 없습니다.")
        List<@NotNull(message = "공용 문제 ID는 null일 수 없습니다.") Long> questionIds
) {
}
