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
@Table(name = "public_question_option")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_question_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PublicQuestion publicQuestion;

    @Column(name = "option_number", nullable = false)
    private short optionNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static PublicQuestionOption create(short optionNumber, String content) {
        return PublicQuestionOption.builder()
                .optionNumber(optionNumber)
                .content(content)
                .build();
    }

    void assignQuestion(PublicQuestion publicQuestion) {
        this.publicQuestion = publicQuestion;
    }
}
