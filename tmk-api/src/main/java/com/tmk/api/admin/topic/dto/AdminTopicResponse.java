package com.tmk.api.admin.topic.dto;

import com.tmk.api.admin.topic.result.AdminTopicResult;
import java.time.OffsetDateTime;

public record AdminTopicResponse(
        Long topicId,
        String name,
        boolean active,
        long questionCount,
        OffsetDateTime createdAt
) {

    public static AdminTopicResponse from(AdminTopicResult result) {
        return new AdminTopicResponse(
                result.topicId(),
                result.name(),
                result.active(),
                result.questionCount(),
                result.createdAt()
        );
    }
}
