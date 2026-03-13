package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.ExamPort;
import com.tmk.core.port.out.QuestionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmitExamService {

    private final ExamPort examPort;
    private final QuestionPort questionPort;
    private final ExamGradingService examGradingService;

    public Exam submit(Long examId, Long userId) {
        // TODO
        return null;
    }
}
