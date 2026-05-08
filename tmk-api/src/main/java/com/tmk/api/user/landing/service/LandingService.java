package com.tmk.api.user.landing.service;

import com.tmk.api.user.landing.result.LandingStatResult;
import com.tmk.core.port.out.persistence.DailyActivityStatPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LandingService {

    private final DailyActivityStatPort dailyActivityStatPort;

    @Transactional(readOnly = true)
    public LandingStatResult getLandingStats() {
        return LandingStatResult.from(dailyActivityStatPort.findSummary());
    }
}
