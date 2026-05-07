package com.tmk.api.monitoring.service;

import com.tmk.core.port.out.persistence.DailyActivityStatPort;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringStatRecorder {

    private final DailyActivityStatPort dailyActivityStatPort;

    @Transactional
    public void recordUserWebAccessAttempt() {
        OffsetDateTime now = OffsetDateTime.now();
        dailyActivityStatPort.increaseUserPageAccessAttemptCount(now.toLocalDate(), 1, now);
    }

    @Transactional
    public void recordExamRun() {
        OffsetDateTime now = OffsetDateTime.now();
        dailyActivityStatPort.increaseExamRunCount(now.toLocalDate(), 1, now);
    }

    @Transactional
    public void recordDocumentRegistration() {
        OffsetDateTime now = OffsetDateTime.now();
        dailyActivityStatPort.increaseDocumentRegistrationCount(now.toLocalDate(), 1, now);
    }

    @Transactional
    public void recordGeneratedPrivateQuestions(int generatedQuestionCount) {
        if (generatedQuestionCount <= 0) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        dailyActivityStatPort.increaseGeneratedPrivateQuestionCount(now.toLocalDate(), generatedQuestionCount, now);
    }
}
