package com.tmk.api.admin.question.request;

import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminPublicQuestionCreateRequest(
        @NotNull(message = "Topic ID는 필수입니다.")
        Long topicId,

        @NotBlank(message = "문제 본문은 필수입니다.")
        String content,

        @NotNull(message = "문제 유형은 필수입니다.")
        QuestionType type,

        @NotNull(message = "난이도는 필수입니다.")
        Difficulty difficulty,

        @NotBlank(message = "정답은 필수입니다.")
        String answer,

        @NotBlank(message = "해설은 필수입니다.")
        String explanation,

        @NotNull(message = "선택지 목록은 필수입니다.")
        List<@NotBlank(message = "선택지 내용은 비어 있을 수 없습니다.") String> options
) {
}
