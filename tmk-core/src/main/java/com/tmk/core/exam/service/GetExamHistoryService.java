package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.port.out.persistence.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetExamHistoryService {

    private final ExamPort examPort;

    public List<Exam> getHistory(Long userId, int page, int size) {
        List<Exam> all = examPort.findByUserIdOrderByCreatedAtDesc(userId);
        int fromIndex = page * size;
        if (fromIndex >= all.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, all.size());
        return all.subList(fromIndex, toIndex);
    }

    public long count(Long userId) {
        return examPort.findByUserIdOrderByCreatedAtDesc(userId).size();
    }
}
