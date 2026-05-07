package com.tmk.api.landing.usecase;

import com.tmk.api.landing.dto.LandingStatResponse;
import com.tmk.api.landing.result.LandingStatResult;
import com.tmk.api.landing.service.LandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LandingUseCase {

    private final LandingService landingService;

    @Transactional(readOnly = true)
    public LandingStatResponse getLandingStats() {
        LandingStatResult result = landingService.getLandingStats();
        return LandingStatResponse.from(result);
    }
}
