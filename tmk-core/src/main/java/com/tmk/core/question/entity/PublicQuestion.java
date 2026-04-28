package com.tmk.core.question.entity;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "public_question")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "created_by_admin_id", nullable = false)
    private Long createdByAdminId;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "publicQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PublicQuestionOption> options = new ArrayList<>();

    public static PublicQuestion create(
            Long topicId,
            Long createdByAdminId,
            String content,
            QuestionType type,
            Difficulty difficulty,
            String answer,
            String explanation,
            List<PublicQuestionOption> options,
            OffsetDateTime now
    ) {
        PublicQuestion question = PublicQuestion.builder()
                .topicId(topicId)
                .createdByAdminId(createdByAdminId)
                .active(true)
                .content(content)
                .type(type)
                .difficulty(difficulty)
                .answer(answer)
                .explanation(explanation)
                .createdAt(now)
                .updatedAt(now)
                .build();
        question.replaceOptions(options);
        question.validateOptionCount();
        return question;
    }

    public void replaceOptions(List<PublicQuestionOption> options) {
        this.options.clear();
        if (options == null) {
            return;
        }
        for (PublicQuestionOption option : options) {
            option.assignQuestion(this);
            this.options.add(option);
        }
    }

    public void validateOptionCount() {
        int optionCount = options.size();
        if (type == QuestionType.MULTIPLE_CHOICE && optionCount != 5) {
            throw new BusinessException(ErrorCode.QUESTION_OPTION_COUNT_INVALID);
        }
        if (type == QuestionType.TRUE_FALSE && optionCount != 2) {
            throw new BusinessException(ErrorCode.QUESTION_OPTION_COUNT_INVALID);
        }
        if (type == QuestionType.SHORT_ANSWER && optionCount != 0) {
            throw new BusinessException(ErrorCode.QUESTION_OPTION_COUNT_INVALID);
        }
    }

    public void activate(OffsetDateTime now) {
        this.active = true;
        this.updatedAt = now;
    }

    public void deactivate(OffsetDateTime now) {
        this.active = false;
        this.updatedAt = now;
    }
}
