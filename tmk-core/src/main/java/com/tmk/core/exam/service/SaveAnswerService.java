package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaveAnswerService {

    private final GetExamService getExamService;
    private final ExamPort examPort;

    public void saveAnswers(Long examId, Long userId, Map<Long, String> answers) {
        Exam exam = getExamService.getExam(examId, userId);

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.EXAM_ALREADY_SUBMITTED);
        }
        if (exam.isExpired()) {
            throw new BusinessException(ErrorCode.EXAM_EXPIRED);
        }

        answers.forEach(exam::saveAnswer);
        examPort.save(exam);
    }
}
