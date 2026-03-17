# SPEC-AUTH-001: Email Verification Service Implementation

---
spec_id: SPEC-AUTH-001
title: Email Verification Service Implementation
created: 2026-03-17
status: Planned
priority: High
lifecycle: spec-first
assigned: manager-ddd
related_specs: []
---

## 1. Environment

- **Project**: TMK (Test My Knowledge) - AI 기반 문제은행 플랫폼
- **Module**: tmk-core (순수 도메인 레이어, Spring/JPA 외부 프레임워크 미의존)
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.5.x
- **Database**: PostgreSQL 14+ (email_verification 테이블)
- **Architecture**: Clean Architecture (Hexagonal) - Port & Adapter 패턴
- **Dependency Direction**: tmk-api -> tmk-core <- tmk-batch (core는 외부 의존 없음)

## 2. Assumptions

- **AS-1**: SMTP 연동 없이 6자리 랜덤 숫자 코드를 생성하여 email_verification 테이블에 저장한다 (개발 단계 MVP — 실제 이메일은 발송하지 않음)
- **AS-2**: 이메일 인증 코드는 5분간 유효하다
- **AS-3**: 동일 이메일로 재전송 시 기존 레코드를 삭제 후 새로 생성한다 (OVERWRITE 정책)
- **AS-4**: 이메일 인증 완료 후에만 회원가입이 가능하다
- **AS-5**: 이미 인증 완료된 이메일은 재인증 불필요하다
- **AS-6**: `EmailVerificationPort` 인터페이스가 이미 존재하며, `findByEmail`, `save`, `deleteByEmail` 메서드를 제공한다
- **AS-7**: `EmailVerification` 엔티티에 `verify()` 도메인 메서드가 이미 존재한다
- **AS-8**: `ErrorCode.INVALID_VERIFICATION_CODE` (AUTH_008, 400)가 이미 정의되어 있다
- **AS-9**: `BusinessException(ErrorCode)` 도메인 예외 패턴이 이미 존재한다

## 3. Requirements

### 3.1 Event-Driven Requirements (WHEN ... THEN ...)

- **REQ-01**: WHEN 사용자가 이메일 인증 코드 전송을 요청하면, THEN 시스템은 해당 이메일의 기존 인증 레코드를 삭제하고, 6자리 랜덤 숫자 코드를 생성하여 email_verification 테이블에 저장해야 한다. (SMTP 미연동 — 이메일은 실제 발송하지 않음)

- **REQ-02**: WHEN 사용자가 올바른 이메일과 인증 코드를 제출하면, THEN 시스템은 해당 인증 레코드의 verified 상태를 true로 변경하고 저장해야 한다.

- **REQ-03**: WHEN 사용자가 존재하지 않는 이메일로 인증을 시도하면, THEN 시스템은 INVALID_VERIFICATION_CODE (AUTH_008) 예외를 발생시켜야 한다.

- **REQ-04**: WHEN 사용자가 만료된 인증 코드로 인증을 시도하면, THEN 시스템은 INVALID_VERIFICATION_CODE (AUTH_008) 예외를 발생시켜야 한다.

- **REQ-05**: WHEN 사용자가 잘못된 인증 코드를 제출하면, THEN 시스템은 INVALID_VERIFICATION_CODE (AUTH_008) 예외를 발생시켜야 한다.

### 3.2 Ubiquitous Requirements (시스템은 항상 ... 해야 한다)

- **REQ-06**: 시스템은 항상 인증 코드 생성 시 만료 시간을 현재 시간 + 5분으로 설정해야 한다.

- **REQ-07**: 시스템은 항상 새 인증 레코드를 verified=false 상태로 생성해야 한다.

- **REQ-08**: 시스템은 항상 인증 레코드 생성 시 createdAt을 현재 시간으로 설정해야 한다.

### 3.3 Unwanted Requirements (시스템은 ... 하지 않아야 한다)

- **REQ-09**: 시스템은 실제 SMTP를 통해 이메일을 전송하지 않아야 한다 (MVP 개발 단계).

## 4. Specifications

### 4.1 SendEmailVerificationService

**위치**: `tmk-core/src/main/java/com/tmk/core/auth/service/SendEmailVerificationService.java`

**sendVerification(String email) 흐름**:

1. `emailVerificationPort.deleteByEmail(email)` - 기존 레코드 삭제
2. 6자리 랜덤 숫자 코드 생성 (`generateCode()` 헬퍼 메서드 — `String.format("%06d", random.nextInt(1_000_000))`)
3. `EmailVerification` 빌더로 새 레코드 생성:
   - code: 생성된 랜덤 코드
   - verified: false
   - expiredAt: OffsetDateTime.now() + 5분
   - createdAt: OffsetDateTime.now()
4. `emailVerificationPort.save(verification)` - email_verification 테이블에 저장
5. (TODO 주석) 이후 SMTP 연동 시 여기서 이메일 발송 추가

### 4.2 VerifyEmailService

**위치**: `tmk-core/src/main/java/com/tmk/core/auth/service/VerifyEmailService.java`

**verify(String email, String code) 흐름**:

1. `emailVerificationPort.findByEmail(email)` -> 없으면 `BusinessException(INVALID_VERIFICATION_CODE)` throw
2. `verification.getExpiredAt().isBefore(OffsetDateTime.now())` -> 만료 시 `BusinessException(INVALID_VERIFICATION_CODE)` throw
3. `!code.equals(verification.getCode())` -> 불일치 시 `BusinessException(INVALID_VERIFICATION_CODE)` throw
4. `verification.verify()` - verified=true 설정
5. `emailVerificationPort.save(verification)` - 저장

### 4.3 AuthUseCase Integration

**위치**: `tmk-api/src/main/java/com/tmk/api/auth/usecase/AuthUseCase.java`

기존 통합 포인트 (이미 구현됨):
```java
public void sendVerification(String email) {
    sendEmailVerificationService.sendVerification(email);
}
public void verify(String email, String code) {
    verifyEmailService.verify(email, code);
}
```

## 5. Out of Scope

- SMTP 이메일 실제 발송 (현재는 코드를 email_verification 테이블에만 저장)
- Rate limiting (인증 코드 전송 횟수 제한)
- Redis 기반 인증 코드 저장 (현재는 PostgreSQL email_verification 테이블 사용)
- REST API 컨트롤러 구현 (별도 SPEC)
- 소셜 로그인 연동

## 6. Traceability

| Requirement | Service | Method | Test |
|-------------|---------|--------|------|
| REQ-01 | SendEmailVerificationService | sendVerification | SendEmailVerificationServiceTest |
| REQ-02 | VerifyEmailService | verify | VerifyEmailServiceTest |
| REQ-03 | VerifyEmailService | verify | VerifyEmailServiceTest |
| REQ-04 | VerifyEmailService | verify | VerifyEmailServiceTest |
| REQ-05 | VerifyEmailService | verify | VerifyEmailServiceTest |
| REQ-06 | SendEmailVerificationService | sendVerification | SendEmailVerificationServiceTest |
| REQ-07 | SendEmailVerificationService | sendVerification | SendEmailVerificationServiceTest |
| REQ-08 | SendEmailVerificationService | sendVerification | SendEmailVerificationServiceTest |
| REQ-09 | SendEmailVerificationService | sendVerification | N/A (design constraint) |
