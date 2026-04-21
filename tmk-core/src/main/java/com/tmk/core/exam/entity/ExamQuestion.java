package com.tmk.core.exam.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "order_num", nullable = false)
    private Short orderNum;

    @Column(name = "my_answer", columnDefinition = "TEXT")
    private String myAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    public void assignExam(Exam exam) {
        this.exam = exam;
    }

    public void saveAnswer(String answer) {
        this.myAnswer = answer;
    }

    public void grade(boolean correct) {
        this.isCorrect = correct;
    }
}
