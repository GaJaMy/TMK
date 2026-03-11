package com.tmk.core.question.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_option")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_number", nullable = false)
    private Short optionNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
