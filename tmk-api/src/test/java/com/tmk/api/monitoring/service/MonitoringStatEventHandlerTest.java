package com.tmk.api.monitoring.service;

import static org.mockito.BDDMockito.then;

import com.tmk.api.monitoring.event.DocumentRegisteredEvent;
import com.tmk.api.monitoring.event.ExamStartedEvent;
import com.tmk.api.monitoring.event.PrivateQuestionsGeneratedEvent;
import com.tmk.api.monitoring.event.UserWebAccessAttemptedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitoringStatEventHandlerTest {

    @Mock
    private MonitoringStatRecorder monitoringStatRecorder;

    @InjectMocks
    private MonitoringStatEventHandler monitoringStatEventHandler;

    @Test
    void handleUserWebAccessAttemptedEventRecordsAccessAttempt() {
        monitoringStatEventHandler.handle(new UserWebAccessAttemptedEvent(7L));

        then(monitoringStatRecorder).should().recordUserWebAccessAttempt();
    }

    @Test
    void handleExamStartedEventRecordsExamRun() {
        monitoringStatEventHandler.handle(new ExamStartedEvent(7L));

        then(monitoringStatRecorder).should().recordExamRun();
    }

    @Test
    void handleDocumentRegisteredEventRecordsDocumentRegistration() {
        monitoringStatEventHandler.handle(new DocumentRegisteredEvent(7L));

        then(monitoringStatRecorder).should().recordDocumentRegistration();
    }

    @Test
    void handlePrivateQuestionsGeneratedEventRecordsGeneratedQuestionCount() {
        monitoringStatEventHandler.handle(new PrivateQuestionsGeneratedEvent(7L, 5));

        then(monitoringStatRecorder).should().recordGeneratedPrivateQuestions(5);
    }
}
