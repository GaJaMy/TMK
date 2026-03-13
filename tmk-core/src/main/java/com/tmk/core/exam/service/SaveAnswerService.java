package com.tmk.core.exam.service;

import com.tmk.core.port.out.ExamPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaveAnswerService {

    private final ExamPort examPort;

    public void saveAnswers(Long examId, Long userId, Map<Long, String> answers) {
        // TODO
    }
}
