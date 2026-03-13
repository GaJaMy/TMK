package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetExamHistoryDetailService {

    private final ExamPort examPort;

    public Exam getHistoryDetail(Long examId, Long userId) {
        return examPort.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
    }
}
