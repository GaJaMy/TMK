package com.tmk.api.user.landing.usecase;

import com.tmk.api.user.landing.dto.LandingStatResponse;
import com.tmk.api.user.landing.result.LandingStatResult;
import com.tmk.api.user.landing.service.LandingService;
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
