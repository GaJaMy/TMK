package com.tmk.core.question.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
