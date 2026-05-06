package com.tmk.api.user.topic.result;

import com.tmk.core.topic.entity.Topic;

public record TopicResult(
        Long topicId,
        String name,
        String description,
        boolean active
) {

    public static TopicResult from(Topic topic) {
        return new TopicResult(
                topic.getId(),
                topic.getName(),
                topic.getDescription(),
                topic.isActive()
        );
    }
}
