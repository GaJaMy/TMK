package com.tmk.api.user.landing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.tmk.api.user.landing.result.LandingStatResult;
import com.tmk.core.port.out.persistence.DailyActivityStatPort;
import com.tmk.core.port.out.persistence.DailyActivityStatPort.DailyActivityStatSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LandingServiceTest {

    @Mock
    private DailyActivityStatPort dailyActivityStatPort;

    @InjectMocks
    private LandingService landingService;

    @Test
    void getLandingStatsReturnsSummary() {
        given(dailyActivityStatPort.findSummary()).willReturn(
                new DailyActivityStatSummary(12L, 34L, 56L, 78L)
        );

        LandingStatResult result = landingService.getLandingStats();

        assertThat(result).isEqualTo(new LandingStatResult(12L, 34L, 56L, 78L));
    }
}
