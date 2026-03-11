package com.tmk.core.exam.service;

import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaveAnswerService {

    private final ExamRepository examRepository;

    public void saveAnswers(Long examId, Long userId, Map<Long, String> answers) {
        // TODO
    }
}
