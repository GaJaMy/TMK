package com.tmk.core.question.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
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
@Table(name = "private_question_option")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivateQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "private_question_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PrivateQuestion privateQuestion;

    @Column(name = "option_number", nullable = false)
    private short optionNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static PrivateQuestionOption create(short optionNumber, String content) {
        return PrivateQuestionOption.builder()
                .optionNumber(optionNumber)
                .content(content)
                .build();
    }

    void assignQuestion(PrivateQuestion privateQuestion) {
        this.privateQuestion = privateQuestion;
    }
}
