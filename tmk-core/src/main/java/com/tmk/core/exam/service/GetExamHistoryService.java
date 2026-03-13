package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetExamHistoryService {

    private final ExamPort examPort;

    public List<Exam> getHistory(Long userId, int page, int size) {
        // TODO
        return null;
    }

    public long count(Long userId) {
        // TODO
        return 0;
    }
}
