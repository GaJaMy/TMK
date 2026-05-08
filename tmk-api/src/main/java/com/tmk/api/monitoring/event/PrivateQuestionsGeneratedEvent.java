package com.tmk.api.monitoring.event;

public record PrivateQuestionsGeneratedEvent(Long userId, int generatedQuestionCount) {
}
