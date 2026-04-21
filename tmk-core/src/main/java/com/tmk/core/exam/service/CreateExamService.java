package com.tmk.core.exam.service;

import com.tmk.core.common.ContentScope;
import com.tmk.core.common.Topic;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.persistence.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateExamService {

    private final ExamCreationService examCreationService;
    private final ExamPort examPort;

    public Exam create(Long userId, ContentScope scope, Topic topic) {
        Exam exam = examCreationService.createExam(userId, scope, topic);
        return examPort.save(exam);
    }
}
