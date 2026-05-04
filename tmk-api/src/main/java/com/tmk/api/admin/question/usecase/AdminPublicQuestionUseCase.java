package com.tmk.api.admin.question.usecase;

import com.tmk.api.admin.question.dto.AdminPublicQuestionDetailResponse;
import com.tmk.api.admin.question.result.AdminPublicQuestionDetailResult;
import com.tmk.api.admin.question.request.AdminPublicQuestionCreateRequest;
import com.tmk.api.admin.question.request.AdminPublicQuestionStatusChangeRequest;
import com.tmk.api.admin.question.dto.AdminPublicQuestionSummaryResponse;
import com.tmk.api.admin.question.result.AdminPublicQuestionResult;
import com.tmk.api.admin.question.service.AdminPublicQuestionService;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPublicQuestionUseCase {

    private final AdminPublicQuestionService adminPublicQuestionService;

    public List<AdminPublicQuestionSummaryResponse> getPublicQuestions(
            Long topicId,
            Difficulty difficulty,
            QuestionType type,
            Boolean active
    ) {
        List<AdminPublicQuestionResult> results =
                adminPublicQuestionService.getPublicQuestions(topicId, difficulty, type, active);
        return results.stream()
                .map(AdminPublicQuestionSummaryResponse::from)
                .toList();
    }

    public AdminPublicQuestionDetailResponse getPublicQuestion(Long questionId) {
        AdminPublicQuestionDetailResult result = adminPublicQuestionService.getPublicQuestion(questionId);
        return AdminPublicQuestionDetailResponse.from(result);
    }

    @Transactional
    public AdminPublicQuestionDetailResponse createPublicQuestion(
            Long createdByAdminId,
            AdminPublicQuestionCreateRequest request
    ) {
        AdminPublicQuestionDetailResult result = adminPublicQuestionService.createPublicQuestion(
                createdByAdminId,
                request.topicId(),
                request.content(),
                request.type(),
                request.difficulty(),
                request.answer(),
                request.explanation(),
                request.options()
        );
        return AdminPublicQuestionDetailResponse.from(result);
    }

    @Transactional
    public AdminPublicQuestionDetailResponse changePublicQuestionStatus(
            Long questionId,
            AdminPublicQuestionStatusChangeRequest request
    ) {
        AdminPublicQuestionDetailResult result =
                adminPublicQuestionService.changePublicQuestionStatus(questionId, request.active());
        return AdminPublicQuestionDetailResponse.from(result);
    }

    @Transactional
    public void deletePublicQuestion(Long questionId) {
        adminPublicQuestionService.deletePublicQuestion(questionId);
    }
}
