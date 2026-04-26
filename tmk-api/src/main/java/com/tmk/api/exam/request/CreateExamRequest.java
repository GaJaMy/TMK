package com.tmk.api.exam.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateExamRequest {
    @Schema(defaultValue = "문제 범위")
    @NotBlank(message = "scope는 필수 정보 입니다.")
    private String scope;


    private String topic;
}
