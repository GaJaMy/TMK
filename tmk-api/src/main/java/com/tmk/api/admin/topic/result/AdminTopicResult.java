package com.tmk.api.admin.topic.result;

import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;

public record AdminTopicResult(
        Long topicId,
        String name,
        boolean active,
        long questionCount,
        OffsetDateTime createdAt
) {

    public static AdminTopicResult from(Topic topic, long questionCount) {
        return new AdminTopicResult(
                topic.getId(),
                topic.getName(),
                topic.isActive(),
                questionCount,
                topic.getCreatedAt()
        );
    }
}
