package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetExamHistoryService {

    private final ExamRepository examRepository;

    public List<Exam> getHistory(Long userId, int page, int size) {
        // TODO
        return List.of();
    }
}
