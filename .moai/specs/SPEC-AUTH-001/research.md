# SPEC-AUTH-001: Research Findings

---
spec_id: SPEC-AUTH-001
title: Email Verification Service Research Summary
---

## 1. Target Files (Stubs)

| File | Status | Notes |
|------|--------|-------|
| `tmk-core/src/main/java/com/tmk/core/auth/service/SendEmailVerificationService.java` | Stub (TODO) | EmailVerificationPort 주입 완료, sendVerification 메서드 비어있음 |
| `tmk-core/src/main/java/com/tmk/core/auth/service/VerifyEmailService.java` | Stub (TODO) | EmailVerificationPort 주입 완료, verify 메서드 비어있음 |

## 2. Available Infrastructure

### EmailVerificationPort (Port Interface)

**Location**: `tmk-core/src/main/java/com/tmk/core/port/out/EmailVerificationPort.java`

```java
public interface EmailVerificationPort {
    Optional<EmailVerification> findByEmail(String email);
    EmailVerification save(EmailVerification emailVerification);
    void deleteByEmail(String email);
}
```

### EmailVerification Entity

**Location**: `tmk-core/src/main/java/com/tmk/core/emailverification/entity/EmailVerification.java`

- Fields: id (Long, IDENTITY), email (String, UNIQUE), code (String, max 10), verified (boolean), expiredAt (OffsetDateTime), createdAt (OffsetDateTime)
- Domain method: `verify()` - sets `this.verified = true`
- Uses @Builder, @Getter, @AllArgsConstructor, @NoArgsConstructor(PROTECTED)

### Error Codes

| Code | Constant | HTTP Status | Message |
|------|----------|-------------|---------|
| AUTH_005 | EMAIL_NOT_VERIFIED | 403 | 이메일 인증이 완료되지 않았습니다 |
| AUTH_008 | INVALID_VERIFICATION_CODE | 400 | 인증 코드가 올바르지 않거나 만료되었습니다 |

### BusinessException

**Location**: `tmk-core/src/main/java/com/tmk/core/exception/BusinessException.java`

- Accepts `ErrorCode` enum
- Standard domain exception pattern

## 3. Business Rules (from Domain Design Docs)

1. **5분 유효**: 인증 코드 생성 시 expiredAt = now + 5분
2. **OVERWRITE 정책**: 동일 이메일 재전송 시 delete + insert
3. **인증 필수**: 회원가입 전 verified=true 필요
4. **재인증 불필요**: 이미 verified=true인 이메일은 재인증 불필요
5. **고정 코드**: 개발 단계에서 "123456" 사용 (SMTP 미연동)

## 4. AuthUseCase Integration Point

**Location**: `tmk-api/src/main/java/com/tmk/api/auth/usecase/AuthUseCase.java`

AuthUseCase가 두 서비스를 호출하는 구조가 이미 존재:
- `sendVerification(email)` -> `SendEmailVerificationService.sendVerification(email)`
- `verify(email, code)` -> `VerifyEmailService.verify(email, code)`

## 5. Test Baseline

- 기존 테스트: 없음 (0%)
- 테스트 위치: `tmk-core/src/test/java/com/tmk/core/auth/service/`
- 테스트 방식: Mockito 기반 단위 테스트 (DDD development_mode)
- 테스트 프레임워크: JUnit 5 + Mockito + AssertJ

## 6. Database Schema

```sql
-- email_verification 테이블 (tech.md 기준)
CREATE TABLE email_verification (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    expired_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Note: tech.md의 DDL과 실제 엔티티 간 약간의 차이 존재 (verified_yn CHAR(1) vs verified BOOLEAN). 엔티티 기준으로 구현.
