package com.tmk.core.exam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "total_questions", nullable = false)
    private Short totalQuestions;

    @Column(name = "time_limit", nullable = false)
    private Short timeLimit;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExamStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "expired_at", nullable = false)
    private OffsetDateTime expiredAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    public void addQuestion(ExamQuestion examQuestion) {
        examQuestion.assignExam(this);
        this.examQuestions.add(examQuestion);
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiredAt);
    }

    public boolean isSubmitted() {
        return status == ExamStatus.SUBMITTED;
    }

    public void submit() {
        if (isSubmitted()) throw new IllegalStateException("이미 제출된 시험입니다.");
        this.status = ExamStatus.SUBMITTED;
        this.submittedAt = OffsetDateTime.now();
    }

    public void saveAnswer(Long questionId, String answer) {
        if (isExpired()) throw new IllegalStateException("시험 시간이 초과되었습니다.");
        if (isSubmitted()) throw new IllegalStateException("이미 제출된 시험입니다.");
        examQuestions.stream()
                .filter(eq -> eq.getQuestionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문제입니다."))
                .saveAnswer(answer);
    }
}
