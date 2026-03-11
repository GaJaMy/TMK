package com.tmk.api.common;

import com.tmk.core.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponse<T> {

    private final String errorCode;
    private final String msg;
    private final T data;

    private ApiResponse(String errorCode, String msg, T data) {
        this.errorCode = errorCode;
        this.msg = msg;
        this.data = data;
    }

    // ── 성공 ──────────────────────────────────────────────────────────────────

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "ok", data));
    }

    public static ResponseEntity<ApiResponse<Void>> ok() {
        return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "ok", null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("SUCCESS", "생성되었습니다.", data));
    }

    public static ResponseEntity<ApiResponse<Void>> noContent() {
        return ResponseEntity.noContent().build();
    }

    // ── 실패 ──────────────────────────────────────────────────────────────────

    public static ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null));
    }

    public static ResponseEntity<ApiResponse<Void>> fail(ErrorCode errorCode, String customMsg) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode.getCode(), customMsg, null));
    }

    // ── 필터/핸들러 내부 직렬화용 (HttpServletResponse에 직접 쓸 때) ──────────

    public static ApiResponse<Void> of(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    // ── Getter ────────────────────────────────────────────────────────────────

    public String getErrorCode() { return errorCode; }
    public String getMsg() { return msg; }
    public T getData() { return data; }
}