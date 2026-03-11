package com.tmk.api.common.exception;

import com.tmk.api.common.ApiResponse;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 도메인 비즈니스 예외 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ApiResponse.fail(e.getErrorCode());
    }

    /** @RequestBody @Valid 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return ApiResponse.fail(ErrorCode.INVALID_INPUT, message);
    }

    /** @Validated 컨트롤러에서 @RequestParam/@PathVariable 검증 실패 (Spring Boot 3.2+) */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return ApiResponse.fail(ErrorCode.INVALID_INPUT, message);
    }

    /** @Validated 서비스 레이어 검증 실패 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return field + ": " + v.getMessage();
                })
                .findFirst()
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return ApiResponse.fail(ErrorCode.INVALID_INPUT, message);
    }

    /** JSON 파싱 실패 (잘못된 타입, 필드명 오류 등) */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ApiResponse.fail(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다.");
    }

    /** 필수 @RequestParam 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return ApiResponse.fail(ErrorCode.INVALID_INPUT, e.getParameterName() + " 파라미터가 필요합니다.");
    }

    /** 그 외 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}