package com.tmk.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT("COMMON_001", "잘못된 입력값입니다.", 400),
    INTERNAL_SERVER_ERROR("COMMON_002", "서버 내부 오류입니다.", 500),

    // Auth
    TOKEN_INVALID("AUTH_001", "유효하지 않은 토큰입니다.", 401),
    TOKEN_EXPIRED("AUTH_002", "만료된 토큰입니다.", 401),
    UNAUTHORIZED("AUTH_003", "인증이 필요합니다.", 401),
    FORBIDDEN("AUTH_004", "접근 권한이 없습니다.", 403),
    EMAIL_NOT_VERIFIED("AUTH_005", "이메일 인증이 완료되지 않았습니다.", 403),
    INVALID_CREDENTIALS("AUTH_006", "이메일 또는 비밀번호가 올바르지 않습니다.", 401),
    DUPLICATE_EMAIL("AUTH_007", "이미 사용 중인 이메일입니다.", 409),
    INVALID_VERIFICATION_CODE("AUTH_008", "인증 코드가 올바르지 않거나 만료되었습니다.", 400),
    REFRESH_TOKEN_INVALID("AUTH_009", "유효하지 않은 리프레시 토큰입니다.", 401),

    // Document
    DOCUMENT_NOT_FOUND("DOCUMENT_001", "문서를 찾을 수 없습니다.", 404),

    // Question
    QUESTION_NOT_FOUND("QUESTION_001", "문제를 찾을 수 없습니다.", 404),
    INSUFFICIENT_QUESTIONS("QUESTION_002", "시험을 생성할 문제가 부족합니다.", 422),

    // Exam
    EXAM_NOT_FOUND("EXAM_001", "시험을 찾을 수 없습니다.", 404),
    EXAM_ALREADY_SUBMITTED("EXAM_002", "이미 제출된 시험입니다.", 409),
    EXAM_EXPIRED("EXAM_003", "시험 시간이 만료되었습니다.", 410),
    EXAM_NOT_IN_PROGRESS("EXAM_004", "진행 중인 시험이 아닙니다.", 400);

    private final String code;
    private final String message;
    private final int status;
}
