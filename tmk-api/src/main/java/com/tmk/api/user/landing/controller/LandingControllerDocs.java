package com.tmk.api.user.landing.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.user.landing.dto.LandingStatResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface LandingControllerDocs {

    @Operation(summary = "랜딩 페이지 누적 통계 조회")
    ResponseEntity<ApiResponse<LandingStatResponse>> getLandingStats();
}
