package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateExamService {

    private final ExamCreationService examCreationService;
    private final ExamRepository examRepository;

    public Exam create(Long userId) {
        Exam exam = examCreationService.createExam(userId);
        return examRepository.save(exam);
    }
}
