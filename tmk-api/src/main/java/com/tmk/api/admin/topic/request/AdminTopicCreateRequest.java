package com.tmk.api.admin.topic.request;

import jakarta.validation.constraints.NotBlank;

public record AdminTopicCreateRequest(
        @NotBlank(message = "Topic 이름은 필수입니다.")
        String name
) {
}
