package com.tmk.api.user.topic.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.user.topic.dto.TopicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface TopicControllerDocs {

    @Operation(summary = "공용 Topic 목록 조회", description = "공용 문제 기반 시험 시작에 사용할 활성 Topic 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<List<TopicResponse>>> getTopics();
}
