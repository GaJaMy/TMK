package com.tmk.api.landing.dto;

import com.tmk.api.landing.result.LandingStatResult;

public record LandingStatResponse(
        long userPageAccessAttemptCount,
        long examRunCount,
        long documentRegistrationCount,
        long generatedPrivateQuestionCount
) {

    public static LandingStatResponse from(LandingStatResult result) {
        return new LandingStatResponse(
                result.userPageAccessAttemptCount(),
                result.examRunCount(),
                result.documentRegistrationCount(),
                result.generatedPrivateQuestionCount()
        );
    }
}
