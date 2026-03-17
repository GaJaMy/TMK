package com.tmk.api.exam.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.exam.dto.AnswerCommand;
import com.tmk.api.exam.dto.ExamDetailResult;
import com.tmk.api.exam.dto.ExamResult;
import com.tmk.api.exam.dto.ExamResultData;
import com.tmk.api.exam.dto.HistoryDetailResult;
import com.tmk.api.exam.dto.HistoryListResult;
import com.tmk.api.exam.dto.SubmitResult;
import com.tmk.api.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Exam", description = "시험 API")
public interface ExamControllerDocs {

    @Operation(summary = "시험 생성", description = "인증된 사용자를 위한 새 시험을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시험 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "문제 수 부족 등 시험 생성 조건 미충족")
    })
    ResponseEntity<ApiResponse<ExamResult>> createExam(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "시험 조회", description = "시험 ID로 시험 정보와 문제 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시험 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ExamDetailResult>> getExam(
            @Parameter(description = "조회할 시험 ID", required = true) Long examId,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "답안 임시 저장", description = "시험 진행 중 답안을 임시로 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답안 저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<Void>> saveAnswers(
            @Parameter(description = "시험 ID", required = true) Long examId,
            @RequestBody(description = "저장할 답안 목록", required = true) List<AnswerCommand> answers,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "시험 제출", description = "시험 답안을 최종 제출하고 채점을 요청합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시험 제출 성공, 채점 결과 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<SubmitResult>> submitExam(
            @Parameter(description = "제출할 시험 ID", required = true) Long examId,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "시험 결과 조회", description = "제출된 시험의 채점 결과를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결과 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 결과를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ExamResultData>> getResult(
            @Parameter(description = "결과를 조회할 시험 ID", required = true) Long examId,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "시험 이력 목록 조회", description = "현재 사용자의 시험 이력을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이력 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ApiResponse<HistoryListResult>> getHistory(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") int size,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "시험 이력 상세 조회", description = "특정 시험의 상세 이력(각 문제별 답안 및 정오 여부 포함)을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이력 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "시험 이력을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<HistoryDetailResult>> getHistoryDetail(
            @Parameter(description = "조회할 시험 ID", required = true) Long examId,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );
}
