package com.tmk.api.user.topic.dto;

import com.tmk.api.user.topic.result.TopicResult;

public record TopicResponse(
        Long topicId,
        String name,
        String description,
        boolean active
) {

    public static TopicResponse from(TopicResult result) {
        return new TopicResponse(
                result.topicId(),
                result.name(),
                result.description(),
                result.active()
        );
    }
}
