package com.tmk.core.exam.entity;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "document_id")
    private Long documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private ExamSourceType sourceType;

    @Column(name = "total_questions", nullable = false)
    private short totalQuestions;

    @Column(name = "time_limit_minutes", nullable = false)
    private short timeLimitMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    public static Exam createPublicTopicExam(
            Long userId,
            Long topicId,
            short totalQuestions,
            short timeLimitMinutes,
            OffsetDateTime now
    ) {
        validateCreateArguments(topicId, totalQuestions, timeLimitMinutes);
        return Exam.builder()
                .userId(userId)
                .topicId(topicId)
                .sourceType(ExamSourceType.PUBLIC_TOPIC)
                .totalQuestions(totalQuestions)
                .timeLimitMinutes(timeLimitMinutes)
                .status(ExamStatus.CREATED)
                .createdAt(now)
                .build();
    }

    public static Exam createPrivateDocumentExam(
            Long userId,
            Long documentId,
            short totalQuestions,
            short timeLimitMinutes,
            OffsetDateTime now
    ) {
        validateCreateArguments(documentId, totalQuestions, timeLimitMinutes);
        return Exam.builder()
                .userId(userId)
                .documentId(documentId)
                .sourceType(ExamSourceType.PRIVATE_DOCUMENT)
                .totalQuestions(totalQuestions)
                .timeLimitMinutes(timeLimitMinutes)
                .status(ExamStatus.CREATED)
                .createdAt(now)
                .build();
    }

    public void addQuestion(ExamQuestion examQuestion) {
        examQuestion.assignExam(this);
        examQuestions.add(examQuestion);
        examQuestions.sort(Comparator.comparing(ExamQuestion::getOrderNum));
    }

    public void start(OffsetDateTime now) {
        if (status != ExamStatus.CREATED) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_STARTED);
        }
        this.status = ExamStatus.IN_PROGRESS;
        this.startedAt = now;
        this.expiredAt = now.plusMinutes(timeLimitMinutes);
    }

    public void saveAnswer(Long examQuestionId, String myAnswer, OffsetDateTime now) {
        validateInProgress(now);
        findExamQuestion(examQuestionId).saveAnswer(myAnswer);
    }

    public void submit(OffsetDateTime now) {
        if (status == ExamStatus.SUBMITTED) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);
        }
        if (status != ExamStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.EXAM_NOT_IN_PROGRESS);
        }
        this.status = ExamStatus.SUBMITTED;
        this.submittedAt = now;
    }

    public boolean isInProgress() {
        return status == ExamStatus.IN_PROGRESS;
    }

    public boolean isExpired(OffsetDateTime now) {
        return expiredAt != null && now.isAfter(expiredAt);
    }

    public long getRemainingSeconds(OffsetDateTime now) {
        if (expiredAt == null) {
            return 0L;
        }
        return Math.max(0L, expiredAt.toEpochSecond() - now.toEpochSecond());
    }

    public void validateInProgress(OffsetDateTime now) {
        if (status != ExamStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.EXAM_NOT_IN_PROGRESS);
        }
        if (isExpired(now)) {
            throw new BusinessException(ErrorCode.EXAM_EXPIRED);
        }
    }

    private ExamQuestion findExamQuestion(Long examQuestionId) {
        return examQuestions.stream()
                .filter(examQuestion -> examQuestion.getId().equals(examQuestionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_QUESTION_NOT_FOUND));
    }

    private static void validateCreateArguments(Long sourceId, short totalQuestions, short timeLimitMinutes) {
        if (sourceId == null || totalQuestions <= 0 || timeLimitMinutes <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
