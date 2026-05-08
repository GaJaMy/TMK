package com.tmk.core.port.out.persistence;

import com.tmk.core.monitoring.entity.DailyActivityStat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyActivityStatPort {

    DailyActivityStat save(DailyActivityStat dailyActivityStat);

    Optional<DailyActivityStat> findByStatDate(LocalDate statDate);

    List<DailyActivityStat> findByStatDateBetweenOrderByStatDateAsc(LocalDate from, LocalDate to);

    DailyActivityStatSummary findSummary();

    void increaseUserPageAccessAttemptCount(LocalDate statDate, int value, OffsetDateTime now);

    void increaseExamRunCount(LocalDate statDate, int value, OffsetDateTime now);

    void increaseDocumentRegistrationCount(LocalDate statDate, int value, OffsetDateTime now);

    void increaseGeneratedPrivateQuestionCount(LocalDate statDate, int value, OffsetDateTime now);

    record DailyActivityStatSummary(
            long userPageAccessAttemptCount,
            long examRunCount,
            long documentRegistrationCount,
            long generatedPrivateQuestionCount
    ) {
    }
}
