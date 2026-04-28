package com.tmk.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT("COMMON_001", "잘못된 입력값입니다.", 400),
    INTERNAL_SERVER_ERROR("COMMON_002", "서버 내부 오류입니다.", 500),

    INVALID_ACCESS_TOKEN("AUTH_001", "유효하지 않은 액세스 토큰입니다.", 401),
    EXPIRED_ACCESS_TOKEN("AUTH_002", "만료된 액세스 토큰입니다.", 401),
    INVALID_REFRESH_TOKEN("AUTH_003", "유효하지 않은 리프레시 토큰입니다.", 401),
    UNAUTHORIZED("AUTH_004", "인증이 필요합니다.", 401),
    FORBIDDEN("AUTH_005", "접근 권한이 없습니다.", 403),
    DUPLICATE_USERNAME("AUTH_006", "이미 사용 중인 아이디입니다.", 409),
    INVALID_USERNAME_OR_PASSWORD("AUTH_007", "아이디 또는 비밀번호가 올바르지 않습니다.", 401),
    USER_NOT_FOUND("AUTH_008", "사용자 계정을 찾을 수 없습니다.", 404),
    ADMIN_NOT_FOUND("AUTH_009", "관리자 계정을 찾을 수 없습니다.", 404),
    USER_ACCOUNT_INACTIVE("AUTH_010", "비활성 사용자 계정입니다.", 403),
    ADMIN_ACCOUNT_INACTIVE("AUTH_011", "비활성 관리자 계정입니다.", 403),
    RESET_PASSWORD_TARGET_NOT_FOUND("AUTH_012", "비밀번호를 재설정할 계정을 찾을 수 없습니다.", 404),

    TOPIC_NOT_FOUND("TOPIC_001", "Topic을 찾을 수 없습니다.", 404),
    DUPLICATE_TOPIC_NAME("TOPIC_002", "이미 존재하는 Topic 이름입니다.", 409),
    TOPIC_HAS_PUBLIC_QUESTIONS("TOPIC_003", "공용 문제가 연결된 Topic은 삭제할 수 없습니다.", 409),
    TOPIC_INACTIVE("TOPIC_004", "비활성 Topic입니다.", 409),

    DOCUMENT_NOT_FOUND("DOCUMENT_001", "문서를 찾을 수 없습니다.", 404),
    DOCUMENT_NOT_READY("DOCUMENT_002", "문서 처리가 아직 완료되지 않았습니다.", 409),
    DOCUMENT_PROCESSING_FAILED("DOCUMENT_003", "문서 처리에 실패했습니다.", 500),
    UNSUPPORTED_DOCUMENT_SOURCE("DOCUMENT_004", "지원하지 않는 문서 형식입니다.", 400),

    PRIVATE_QUESTION_NOT_FOUND("QUESTION_001", "개인 문제를 찾을 수 없습니다.", 404),
    PUBLIC_QUESTION_NOT_FOUND("QUESTION_002", "공용 문제를 찾을 수 없습니다.", 404),
    PRIVATE_QUESTION_NOT_ENOUGH("QUESTION_003", "시험에 사용할 개인 문제가 부족합니다.", 422),
    PUBLIC_QUESTION_NOT_ENOUGH("QUESTION_004", "시험에 사용할 공용 문제가 부족합니다.", 422),
    QUESTION_OPTION_COUNT_INVALID("QUESTION_005", "문제 유형에 맞는 선택지 수가 아닙니다.", 400),
    PUBLIC_QUESTION_INACTIVE("QUESTION_006", "비활성 공용 문제입니다.", 409),

    EXAM_NOT_FOUND("EXAM_001", "시험을 찾을 수 없습니다.", 404),
    EXAM_ALREADY_STARTED("EXAM_002", "이미 시작된 시험입니다.", 409),
    EXAM_NOT_IN_PROGRESS("EXAM_003", "진행 중인 시험이 아닙니다.", 409),
    EXAM_EXPIRED("EXAM_004", "시험 시간이 만료되었습니다.", 410),
    EXAM_ALREADY_SUBMITTED("EXAM_005", "이미 제출된 시험입니다.", 409),
    EXAM_IN_PROGRESS_ALREADY_EXISTS("EXAM_006", "이미 진행 중인 시험이 있습니다.", 409),
    EXAM_QUESTION_NOT_FOUND("EXAM_007", "시험 문항을 찾을 수 없습니다.", 404),
    EXAM_QUESTION_REFERENCE_INVALID("EXAM_008", "시험 문항 참조 정보가 올바르지 않습니다.", 500),

    INVALID_MONITORING_PERIOD("MONITORING_001", "모니터링 조회 기간이 올바르지 않습니다.", 400);

    private final String code;
    private final String message;
    private final int status;
}
