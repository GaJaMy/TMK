package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetExamResultService {

    private final ExamPort examPort;

    public Exam getResult(Long examId, Long userId) {
        // TODO
        return null;
    }
}
