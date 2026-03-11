package com.tmk.api.exam.usecase;

import com.tmk.api.exam.dto.*;
import com.tmk.core.exam.service.*;
import com.tmk.core.exam.entity.Exam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExamUseCase {

    private final CreateExamService createExamService;
    private final GetExamService getExamService;
    private final SaveAnswerService saveAnswerService;
    private final SubmitExamService submitExamService;
    private final GetExamResultService getExamResultService;
    private final GetExamHistoryService getExamHistoryService;
    private final GetExamHistoryDetailService getExamHistoryDetailService;

    public ExamResult create(Long userId) {
        createExamService.create(userId);
        // TODO: convert Exam to ExamResult
        return null;
    }

    public ExamDetailResult getExam(Long examId, Long userId) {
        getExamService.getExam(examId, userId);
        // TODO: convert Exam to ExamDetailResult
        return null;
    }

    public void saveAnswers(Long examId, Long userId, List<AnswerCommand> answers) {
        // TODO: convert AnswerCommand list to Map<Long, String>
    }

    public SubmitResult submit(Long examId, Long userId) {
        submitExamService.submit(examId, userId);
        // TODO: build SubmitResult
        return null;
    }

    public ExamResultData getResult(Long examId, Long userId) {
        getExamResultService.getResult(examId, userId);
        // TODO: convert Exam to ExamResultData
        return null;
    }

    public HistoryListResult getHistory(Long userId, int page, int size) {
        getExamHistoryService.getHistory(userId, page, size);
        // TODO: convert List<Exam> to HistoryListResult
        return null;
    }

    public HistoryDetailResult getHistoryDetail(Long examId, Long userId) {
        getExamHistoryDetailService.getHistoryDetail(examId, userId);
        // TODO: convert Exam to HistoryDetailResult
        return null;
    }
}
