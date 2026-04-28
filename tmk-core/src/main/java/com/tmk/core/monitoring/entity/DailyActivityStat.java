package com.tmk.core.monitoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_activity_stat")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyActivityStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false, unique = true)
    private LocalDate statDate;

    @Column(name = "user_page_access_attempt_count", nullable = false)
    private int userPageAccessAttemptCount;

    @Column(name = "exam_run_count", nullable = false)
    private int examRunCount;

    @Column(name = "document_registration_count", nullable = false)
    private int documentRegistrationCount;

    @Column(name = "generated_private_question_count", nullable = false)
    private int generatedPrivateQuestionCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static DailyActivityStat create(LocalDate statDate, OffsetDateTime now) {
        return DailyActivityStat.builder()
                .statDate(statDate)
                .userPageAccessAttemptCount(0)
                .examRunCount(0)
                .documentRegistrationCount(0)
                .generatedPrivateQuestionCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void increaseUserPageAccessAttemptCount(int value, OffsetDateTime now) {
        this.userPageAccessAttemptCount += value;
        this.updatedAt = now;
    }

    public void increaseExamRunCount(int value, OffsetDateTime now) {
        this.examRunCount += value;
        this.updatedAt = now;
    }

    public void increaseDocumentRegistrationCount(int value, OffsetDateTime now) {
        this.documentRegistrationCount += value;
        this.updatedAt = now;
    }

    public void increaseGeneratedPrivateQuestionCount(int value, OffsetDateTime now) {
        this.generatedPrivateQuestionCount += value;
        this.updatedAt = now;
    }
}
