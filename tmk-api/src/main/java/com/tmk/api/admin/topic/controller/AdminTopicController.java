package com.tmk.api.admin.topic.controller;

import com.tmk.api.admin.topic.dto.AdminTopicResponse;
import com.tmk.api.admin.topic.request.AdminTopicCreateRequest;
import com.tmk.api.admin.topic.usecase.AdminTopicUseCase;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.common.ApiVersion;
import com.tmk.api.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminTopicController implements AdminTopicControllerDocs {

    private final AdminTopicUseCase adminTopicUseCase;

    @PostMapping(ApiVersion.V1 + "/topics")
    @Override
    public ResponseEntity<ApiResponse<AdminTopicResponse>> createTopic(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AdminTopicCreateRequest request
    ) {
        return ApiResponse.ok(adminTopicUseCase.createTopic(principal.getPrincipalId(), request));
    }

    @DeleteMapping(ApiVersion.V1 + "/topics/{topicId}")
    @Override
    public ResponseEntity<ApiResponse<Void>> deleteTopic(
            @PathVariable("topicId") Long topicId
    ) {
        adminTopicUseCase.deleteTopic(topicId);
        return ApiResponse.noContent();
    }
}
