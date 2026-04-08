package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ExamPort;
import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmitExamService {

    private final GetExamService getExamService;
    private final ExamPort examPort;
    private final QuestionPort questionPort;
    private final ExamGradingService examGradingService;

    public Exam submit(Long examId, Long userId) {
        Exam exam = getExamService.getExam(examId, userId);

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);
        }
        if (exam.isExpired()) {
            throw new BusinessException(ErrorCode.EXAM_EXPIRED);
        }

        List<Long> questionIds = exam.getExamQuestions().stream()
                .map(eq -> eq.getQuestionId())
                .toList();
        List<Question> questions = questionIds.stream()
                .map(qid -> questionPort.findById(qid)
                        .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND)))
                .toList();

        examGradingService.grade(exam, questions);
        exam.submit();

        return examPort.save(exam);
    }
}
