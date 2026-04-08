package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamStatus;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetExamResultService {

    private final GetExamService getExamService;
    private final ExamPort examPort;

    public Exam getResult(Long examId, Long userId) {
        Exam exam = getExamService.getExam(examId, userId);

        if (exam.getStatus() != ExamStatus.SUBMITTED && exam.getStatus() != ExamStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.EXAM_NOT_IN_PROGRESS);
        }

        return exam;
    }
}
