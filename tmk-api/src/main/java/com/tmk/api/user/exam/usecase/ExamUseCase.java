package com.tmk.api.user.exam.usecase;

import com.tmk.api.user.exam.command.ExamAnswerSaveCommand;
import com.tmk.api.user.exam.dto.ExamCreateResponse;
import com.tmk.api.user.exam.dto.ExamDetailResponse;
import com.tmk.api.user.exam.dto.ExamHistorySummaryResponse;
import com.tmk.api.user.exam.dto.ExamResultDetailResponse;
import com.tmk.api.user.exam.dto.ExamSummaryResponse;
import com.tmk.api.user.exam.dto.ExamStartResponse;
import com.tmk.api.user.exam.request.ExamAnswerSaveRequest;
import com.tmk.api.user.exam.request.ExamCreateRequest;
import com.tmk.api.user.exam.result.ExamCreateResult;
import com.tmk.api.user.exam.result.ExamDetailResult;
import com.tmk.api.user.exam.result.ExamHistorySummaryResult;
import com.tmk.api.user.exam.result.ExamResultDetailResult;
import com.tmk.api.user.exam.result.ExamSummaryResult;
import com.tmk.api.user.exam.result.ExamStartResult;
import com.tmk.api.user.exam.service.ExamService;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ExamUseCase {

    private final ExamService examService;

    @Transactional(readOnly = true)
    public List<ExamSummaryResponse> getExams(Long userId) {
        List<ExamSummaryResult> results = examService.getExams(userId);
        return results.stream()
                .map(ExamSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamHistorySummaryResponse> getExamHistory(Long userId) {
        List<ExamHistorySummaryResult> results = examService.getExamHistory(userId);
        return results.stream()
                .map(ExamHistorySummaryResponse::from)
                .toList();
    }

    @Transactional
    public ExamDetailResponse getExam(Long userId, Long examId) {
        ExamDetailResult result = examService.getExam(userId, examId);
        return ExamDetailResponse.from(result);
    }

    @Transactional(readOnly = true)
    public ExamResultDetailResponse getExamResult(Long userId, Long examId) {
        ExamResultDetailResult result = examService.getExamResult(userId, examId);
        return ExamResultDetailResponse.from(result);
    }

    @Transactional
    public void saveAnswers(Long userId, Long examId, List<ExamAnswerSaveRequest> requests) {
        List<ExamAnswerSaveRequest> answerRequests = requests;
        List<ExamAnswerSaveCommand> commands = answerRequests.stream()
                .map(request -> new ExamAnswerSaveCommand(request.questionId(), request.answer()))
                .toList();
        examService.saveAnswers(userId, examId, commands);
    }

    @Transactional
    public void submitExam(Long userId, Long examId) {
        examService.submitExam(userId, examId);
    }

    @Transactional
    public ExamCreateResponse createExam(Long userId, ExamCreateRequest request) {
        ExamCreateResult result = switch (request.sourceType()) {
            case PUBLIC_TOPIC -> examService.createPublicTopicExam(
                    userId,
                    requireSourceId(request.topicId()),
                    request.questionCount(),
                    request.timeLimitMinutes()
            );
            case PRIVATE_DOCUMENT -> examService.createPrivateDocumentExam(
                    userId,
                    requireSourceId(request.documentId()),
                    request.questionCount(),
                    request.timeLimitMinutes()
            );
        };
        return ExamCreateResponse.from(result);
    }

    @Transactional
    public ExamStartResponse startExam(Long userId, Long examId) {
        ExamStartResult result = examService.startExam(userId, examId);
        return ExamStartResponse.from(result);
    }

    private Long requireSourceId(Long sourceId) {
        if (sourceId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return sourceId;
    }
}
