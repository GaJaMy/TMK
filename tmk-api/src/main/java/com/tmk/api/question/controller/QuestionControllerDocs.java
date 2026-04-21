package com.tmk.api.question.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.question.dto.QuestionDetailResult;
import com.tmk.api.question.dto.QuestionListResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Question", description = "문제 API")
public interface QuestionControllerDocs {

    @Operation(summary = "문제 목록 조회", description = "유형 및 난이도 필터를 적용하여 문제 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    ResponseEntity<ApiResponse<QuestionListResult>> getList(
            @Parameter(description = "문제 유형 (예: MULTIPLE_CHOICE, SHORT_ANSWER, TRUE_FALSE)")
            String type,
            @Parameter(description = "난이도 (예: EASY, MEDIUM, HARD)")
            String difficulty,
            @Parameter(description = "주제 (예: SPRING, JAVA, DATABASE)")
            String topic,
            @Parameter(description = "문제 범위 (PUBLIC 또는 PRIVATE)")
            String scope,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @Parameter(description = "페이지 크기", example = "20")
            int size,
            @Parameter(hidden = true) com.tmk.api.security.CustomUserDetails userDetails
    );

    @Operation(summary = "문제 상세 조회", description = "단일 문제의 상세 정보(선택지, 정답, 해설 포함)를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<QuestionDetailResult>> getDetail(
            @Parameter(description = "조회할 문제 ID", required = true) Long questionId,
            @Parameter(hidden = true) com.tmk.api.security.CustomUserDetails userDetails
    );
}
