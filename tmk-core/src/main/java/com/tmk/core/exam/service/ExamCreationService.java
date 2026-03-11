package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamCreationService {

    private static final int MIN_QUESTIONS = 10;
    private static final short DEFAULT_TIME_LIMIT = 30;

    private final QuestionRepository questionRepository;

    public Exam createExam(Long userId) {
        Map<Difficulty, List<Question>> grouped = questionRepository.findGroupedByDifficulty();

        List<Long> selected = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            List<Question> questions = new ArrayList<>(grouped.getOrDefault(difficulty, Collections.emptyList()));
            if (questions.isEmpty()) {
                throw new IllegalStateException("난이도 " + difficulty + " 문제가 부족합니다.");
            }
            Collections.shuffle(questions);
            selected.add(questions.get(0).getId());
        }

        List<Question> remaining = grouped.values().stream()
                .flatMap(List::stream)
                .filter(q -> !selected.contains(q.getId()))
                .collect(Collectors.toList());

        Collections.shuffle(remaining);
        int needed = MIN_QUESTIONS - selected.size();
        remaining.stream().limit(needed).map(Question::getId).forEach(selected::add);

        Collections.shuffle(selected);

        OffsetDateTime now = OffsetDateTime.now();
        List<ExamQuestion> examQuestions = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            examQuestions.add(ExamQuestion.builder()
                    .questionId(selected.get(i))
                    .orderNum((short) (i + 1))
                    .build());
        }

        return Exam.builder()
                .userId(userId)
                .totalQuestions((short) selected.size())
                .timeLimit(DEFAULT_TIME_LIMIT)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .expiredAt(now.plusMinutes(DEFAULT_TIME_LIMIT))
                .createdAt(now)
                .examQuestions(examQuestions)
                .build();
    }
}
