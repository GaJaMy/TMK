package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ExamPort;
import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmitExamService {

    private final ExamPort examPort;
    private final QuestionPort questionPort;
    private final ExamGradingService examGradingService;

    public Exam submit(Long examId, Long userId) {
        Exam exam = examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        if (exam.isSubmitted()) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);
        }

        // Load questions for grading
        List<Long> questionIds = exam.getExamQuestions().stream()
                .map(eq -> eq.getQuestionId())
                .collect(Collectors.toList());
        List<Question> questions = questionIds.stream()
                .map(id -> questionPort.findById(id).orElseThrow())
                .collect(Collectors.toList());

        // Grade the exam
        examGradingService.grade(exam, questions);

        // Submit
        exam.submit();

        return examPort.save(exam);
    }
}
