package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetExamResultService {

    private final ExamRepository examRepository;

    public Exam getResult(Long examId, Long userId) {
        // TODO
        return null;
    }
}
