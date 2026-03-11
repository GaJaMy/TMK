package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetExamHistoryDetailService {

    private final ExamRepository examRepository;

    public Exam getHistoryDetail(Long examId, Long userId) {
        // TODO
        return null;
    }
}
