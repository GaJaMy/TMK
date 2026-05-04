package com.tmk.api.admin.topic.controller;

import com.tmk.api.admin.topic.dto.AdminTopicResponse;
import com.tmk.api.admin.topic.request.AdminTopicCreateRequest;
import com.tmk.api.common.ApiResponse;
import com.tmk.api.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface AdminTopicControllerDocs {

    @Operation(summary = "Topic 생성", description = "현재 로그인한 관리자가 새 Topic을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 Topic 이름")
    })
    ResponseEntity<ApiResponse<AdminTopicResponse>> createTopic(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AdminTopicCreateRequest request
    );

    @Operation(summary = "Topic 삭제", description = "Topic을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Topic 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "공용 문제가 연결된 Topic")
    })
    ResponseEntity<ApiResponse<Void>> deleteTopic(
            @PathVariable("topicId") Long topicId
    );
}
