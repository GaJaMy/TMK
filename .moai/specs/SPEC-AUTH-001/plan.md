# SPEC-AUTH-001: Implementation Plan

---
spec_id: SPEC-AUTH-001
title: Email Verification Service Implementation Plan
---

## 1. Milestones

### Primary Goal: Core Service Implementation

**SendEmailVerificationService 구현**
- [ ] `sendVerification(String email)` 메서드 구현
- [ ] 기존 레코드 삭제 로직 (`deleteByEmail`)
- [ ] 고정 코드 "123456"으로 `EmailVerification` 빌더 생성
- [ ] 만료 시간 설정 (현재 시간 + 5분)
- [ ] `save()` 호출

**VerifyEmailService 구현**
- [ ] `verify(String email, String code)` 메서드 구현
- [ ] 이메일로 인증 레코드 조회 (`findByEmail`)
- [ ] 존재하지 않을 경우 `BusinessException(INVALID_VERIFICATION_CODE)` throw
- [ ] 만료 시간 검증 (`expiredAt.isBefore(now)`)
- [ ] 코드 일치 검증
- [ ] `verification.verify()` 호출 후 `save()`

### Secondary Goal: Test Coverage

**SendEmailVerificationServiceTest**
- [ ] 정상 전송 시나리오 (새 이메일)
- [ ] 기존 레코드 존재 시 덮어쓰기 시나리오
- [ ] 생성된 레코드의 필드 값 검증 (code, verified, expiredAt, createdAt)

**VerifyEmailServiceTest**
- [ ] 정상 인증 시나리오
- [ ] 존재하지 않는 이메일 시나리오
- [ ] 만료된 코드 시나리오
- [ ] 잘못된 코드 시나리오
- [ ] 인증 후 verified=true 상태 검증

## 2. Technical Approach

### Architecture

```
AuthUseCase (tmk-api)
    |
    v
SendEmailVerificationService / VerifyEmailService (tmk-core, domain service)
    |
    v
EmailVerificationPort (tmk-core, port/out)
    |
    v
EmailVerificationRepository (tmk-api, adapter/out, JPA 구현체)
```

### Design Decisions

1. **고정 코드 사용**: MVP 단계에서 SMTP 의존성 제거. 향후 `CodeGenerator` 인터페이스 도입으로 전략 패턴 적용 가능.
2. **시간 처리**: `OffsetDateTime.now()` 직접 사용. 테스트 용이성을 위해 `Clock` 주입은 이 SPEC 범위 외.
3. **에러 코드 통합**: 모든 인증 실패 케이스에 동일한 `INVALID_VERIFICATION_CODE` 사용 (보안상 세부 실패 원인 미노출).
4. **OVERWRITE 정책**: 동일 이메일 재전송 시 delete + insert. 이메일 컬럼 UNIQUE 제약조건 충돌 방지.

### Dependencies (Existing, No New Additions)

| Dependency | Module | Purpose |
|------------|--------|---------|
| EmailVerificationPort | tmk-core/port/out | Repository 추상화 |
| EmailVerification | tmk-core/emailverification/entity | 도메인 엔티티 |
| BusinessException | tmk-core/exception | 도메인 예외 |
| ErrorCode | tmk-core/exception | 에러 코드 Enum |
| Lombok | tmk-core | @RequiredArgsConstructor |

### Test Strategy

- **테스트 유형**: 단위 테스트 (Mockito 기반)
- **테스트 위치**: `tmk-core/src/test/java/com/tmk/core/auth/service/`
- **Mocking**: `EmailVerificationPort`를 Mockito로 mocking
- **검증**: ArgumentCaptor를 사용하여 `save()` 호출 시 전달된 엔티티 검증
- **커버리지 목표**: 해당 서비스 클래스 100% 라인 커버리지

## 3. Risks and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| OffsetDateTime.now() 테스트 불안정 | 시간 비교 테스트 실패 가능 | ArgumentCaptor로 저장된 엔티티의 expiredAt이 현재 시간 + 5분 범위 내인지 검증 |
| 고정 코드 프로덕션 노출 | 보안 취약점 | AS-1에 명시: MVP 전용. 프로덕션 전 랜덤 코드 생성기 교체 필요 |
| deleteByEmail + save 비원자적 | 동시 요청 시 데이터 불일치 | email UNIQUE 제약조건으로 방어. 트랜잭션 범위는 UseCase 레벨에서 관리 |

## 4. Affected Files

### Modified (구현)
- `tmk-core/src/main/java/com/tmk/core/auth/service/SendEmailVerificationService.java`
- `tmk-core/src/main/java/com/tmk/core/auth/service/VerifyEmailService.java`

### Created (테스트)
- `tmk-core/src/test/java/com/tmk/core/auth/service/SendEmailVerificationServiceTest.java`
- `tmk-core/src/test/java/com/tmk/core/auth/service/VerifyEmailServiceTest.java`

### Unchanged (참조만)
- `tmk-core/src/main/java/com/tmk/core/port/out/EmailVerificationPort.java`
- `tmk-core/src/main/java/com/tmk/core/emailverification/entity/EmailVerification.java`
- `tmk-core/src/main/java/com/tmk/core/exception/ErrorCode.java`
- `tmk-core/src/main/java/com/tmk/core/exception/BusinessException.java`
