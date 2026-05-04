package com.tmk.api.admin.topic.result;

import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;

public record AdminTopicResult(
        Long topicId,
        String name,
        boolean active,
        OffsetDateTime createdAt
) {

    public static AdminTopicResult from(Topic topic) {
        return new AdminTopicResult(
                topic.getId(),
                topic.getName(),
                topic.isActive(),
                topic.getCreatedAt()
        );
    }
}
