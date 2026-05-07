package com.tmk.api.monitoring.service;

import com.tmk.api.monitoring.event.DocumentRegisteredEvent;
import com.tmk.api.monitoring.event.ExamStartedEvent;
import com.tmk.api.monitoring.event.PrivateQuestionsGeneratedEvent;
import com.tmk.api.monitoring.event.UserWebAccessAttemptedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MonitoringStatEventHandler {

    private final MonitoringStatRecorder monitoringStatRecorder;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserWebAccessAttemptedEvent event) {
        monitoringStatRecorder.recordUserWebAccessAttempt();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ExamStartedEvent event) {
        monitoringStatRecorder.recordExamRun();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentRegisteredEvent event) {
        monitoringStatRecorder.recordDocumentRegistration();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PrivateQuestionsGeneratedEvent event) {
        monitoringStatRecorder.recordGeneratedPrivateQuestions(event.generatedQuestionCount());
    }
}
