package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaveAnswerService {

    private final ExamPort examPort;

    public void saveAnswers(Long examId, Long userId, Map<Long, String> answers) {
        Exam exam = examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        answers.forEach(exam::saveAnswer);
        examPort.save(exam);
    }
}
