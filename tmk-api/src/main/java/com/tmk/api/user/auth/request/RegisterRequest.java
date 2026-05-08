package com.tmk.api.user.auth.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "아이디는 필수입니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password,

        @NotBlank(message = "국가 코드는 필수입니다.")
        String countryCode
) {
}
