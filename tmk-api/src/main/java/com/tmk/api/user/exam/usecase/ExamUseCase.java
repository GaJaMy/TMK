package com.tmk.api.user.exam.usecase;

import com.tmk.api.user.exam.dto.ExamCreateResponse;
import com.tmk.api.user.exam.dto.ExamSummaryResponse;
import com.tmk.api.user.exam.dto.ExamStartResponse;
import com.tmk.api.user.exam.request.ExamCreateRequest;
import com.tmk.api.user.exam.result.ExamCreateResult;
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
