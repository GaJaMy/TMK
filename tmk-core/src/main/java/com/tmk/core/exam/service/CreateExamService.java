package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateExamService {

    private final ExamCreationService examCreationService;
    private final ExamPort examPort;

    public Exam create(Long userId) {
        Exam exam = examCreationService.createExam(userId);
        return examPort.save(exam);
    }
}
