package com.tmk.api.monitoring.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import com.tmk.core.port.out.persistence.DailyActivityStatPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitoringStatRecorderTest {

    @Mock
    private DailyActivityStatPort dailyActivityStatPort;

    @InjectMocks
    private MonitoringStatRecorder monitoringStatRecorder;

    @Test
    void recordUserWebAccessAttemptIncreasesAccessAttemptCount() {
        monitoringStatRecorder.recordUserWebAccessAttempt();

        then(dailyActivityStatPort).should().increaseUserPageAccessAttemptCount(any(), eq(1), any());
    }

    @Test
    void recordExamRunIncreasesExamRunCount() {
        monitoringStatRecorder.recordExamRun();

        then(dailyActivityStatPort).should().increaseExamRunCount(any(), eq(1), any());
    }

    @Test
    void recordDocumentRegistrationIncreasesDocumentRegistrationCount() {
        monitoringStatRecorder.recordDocumentRegistration();

        then(dailyActivityStatPort).should().increaseDocumentRegistrationCount(any(), eq(1), any());
    }

    @Test
    void recordGeneratedPrivateQuestionsIncreasesGeneratedQuestionCount() {
        monitoringStatRecorder.recordGeneratedPrivateQuestions(5);

        then(dailyActivityStatPort).should().increaseGeneratedPrivateQuestionCount(any(), eq(5), any());
    }
}
