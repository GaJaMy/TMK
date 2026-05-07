package com.tmk.api.landing.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.landing.dto.LandingStatResponse;
import com.tmk.api.landing.usecase.LandingUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LandingController implements LandingControllerDocs {

    private final LandingUseCase landingUseCase;

    @GetMapping("/landing/stats")
    @Override
    public ResponseEntity<ApiResponse<LandingStatResponse>> getLandingStats() {
        return ApiResponse.ok(landingUseCase.getLandingStats());
    }
}
