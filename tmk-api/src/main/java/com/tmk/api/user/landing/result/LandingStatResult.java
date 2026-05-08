package com.tmk.api.user.landing.result;

import com.tmk.core.port.out.persistence.DailyActivityStatPort.DailyActivityStatSummary;

public record LandingStatResult(
        long userPageAccessAttemptCount,
        long examRunCount,
        long documentRegistrationCount,
        long generatedPrivateQuestionCount
) {

    public static LandingStatResult from(DailyActivityStatSummary summary) {
        return new LandingStatResult(
                summary.userPageAccessAttemptCount(),
                summary.examRunCount(),
                summary.documentRegistrationCount(),
                summary.generatedPrivateQuestionCount()
        );
    }
}
