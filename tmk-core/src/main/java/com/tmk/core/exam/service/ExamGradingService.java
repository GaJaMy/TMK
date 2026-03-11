package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamGradingService {

    public int grade(Exam exam, List<Question> questions) {
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int correctCount = 0;
        for (ExamQuestion examQuestion : exam.getExamQuestions()) {
            Question question = questionMap.get(examQuestion.getQuestionId());
            if (question == null) continue;

            boolean correct = question.getAnswer().equals(examQuestion.getMyAnswer());
            examQuestion.grade(correct);
            if (correct) correctCount++;
        }

        return correctCount;
    }
}
