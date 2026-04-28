package com.tmk.core.exam.entity;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.question.entity.QuestionScope;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_question")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Exam exam;

    @Column(name = "public_question_id")
    private Long publicQuestionId;

    @Column(name = "private_question_id")
    private Long privateQuestionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_scope", nullable = false, length = 20)
    private QuestionScope questionScope;

    @Column(name = "order_num", nullable = false)
    private short orderNum;

    @Column(name = "my_answer", columnDefinition = "TEXT")
    private String myAnswer;

    @Column(name = "is_correct")
    private Boolean correct;

    public static ExamQuestion createPublic(short orderNum, Long publicQuestionId) {
        return ExamQuestion.builder()
                .publicQuestionId(publicQuestionId)
                .questionScope(QuestionScope.PUBLIC)
                .orderNum(orderNum)
                .build();
    }

    public static ExamQuestion createPrivate(short orderNum, Long privateQuestionId) {
        return ExamQuestion.builder()
                .privateQuestionId(privateQuestionId)
                .questionScope(QuestionScope.PRIVATE)
                .orderNum(orderNum)
                .build();
    }

    void assignExam(Exam exam) {
        this.exam = exam;
    }

    public void saveAnswer(String myAnswer) {
        this.myAnswer = myAnswer;
    }

    public void grade(boolean correct) {
        this.correct = correct;
    }

    public Long getQuestionReferenceId() {
        if (questionScope == QuestionScope.PUBLIC) {
            return publicQuestionId;
        }
        if (questionScope == QuestionScope.PRIVATE) {
            return privateQuestionId;
        }
        throw new BusinessException(ErrorCode.EXAM_QUESTION_REFERENCE_INVALID);
    }
}
