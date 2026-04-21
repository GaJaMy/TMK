package com.tmk.core.exam.service;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
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

    private final QuestionPort questionPort;

    public Exam createExam(Long userId, ContentScope scope, Topic topic) {
        Long ownerUserId = scope == ContentScope.PRIVATE ? userId : null;
        Map<Difficulty, List<Question>> grouped = questionPort.findGroupedByDifficulty(scope, ownerUserId, topic);

        List<Long> selected = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            List<Question> questions = new ArrayList<>(grouped.getOrDefault(difficulty, Collections.emptyList()));
            if (questions.isEmpty()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_QUESTIONS);
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
        if (remaining.size() < needed) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_QUESTIONS);
        }
        remaining.stream().limit(needed).map(Question::getId).forEach(selected::add);

        Collections.shuffle(selected);

        OffsetDateTime now = OffsetDateTime.now();
        Exam exam = Exam.builder()
                .userId(userId)
                .totalQuestions((short) selected.size())
                .timeLimit(DEFAULT_TIME_LIMIT)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .expiredAt(now.plusMinutes(DEFAULT_TIME_LIMIT))
                .createdAt(now)
                .build();

        for (int i = 0; i < selected.size(); i++) {
            exam.addQuestion(ExamQuestion.builder()
                    .questionId(selected.get(i))
                    .orderNum((short) (i + 1))
                    .build());
        }

        return exam;
    }
}
