package com.tmk.core.document.entity;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private DocumentSourceType sourceType;

    @Column(name = "source_reference", nullable = false, length = 1000)
    private String sourceReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "generated_question_count", nullable = false)
    private int generatedQuestionCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static Document create(
            Long userId,
            String title,
            DocumentSourceType sourceType,
            String sourceReference,
            OffsetDateTime now
    ) {
        return Document.builder()
                .userId(userId)
                .title(title)
                .sourceType(sourceType)
                .sourceReference(sourceReference)
                .status(DocumentStatus.PROCESSING)
                .generatedQuestionCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void complete(int generatedQuestionCount, OffsetDateTime now) {
        validateQuestionCount(generatedQuestionCount);
        this.status = DocumentStatus.COMPLETED;
        this.generatedQuestionCount = generatedQuestionCount;
        this.updatedAt = now;
    }

    public void fail(OffsetDateTime now) {
        this.status = DocumentStatus.FAILED;
        this.updatedAt = now;
    }

    public void changeSourceReference(String sourceReference, OffsetDateTime now) {
        this.sourceReference = sourceReference;
        this.updatedAt = now;
    }

    public boolean isCompleted() {
        return status == DocumentStatus.COMPLETED;
    }

    public void validateReadyForExam() {
        if (!isCompleted()) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_READY);
        }
        if (generatedQuestionCount <= 0) {
            throw new BusinessException(ErrorCode.PRIVATE_QUESTION_NOT_ENOUGH);
        }
    }

    private void validateQuestionCount(int generatedQuestionCount) {
        if (generatedQuestionCount < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
