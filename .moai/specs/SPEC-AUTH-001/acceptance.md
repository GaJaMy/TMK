# SPEC-AUTH-001: Acceptance Criteria

---
spec_id: SPEC-AUTH-001
title: Email Verification Service Acceptance Criteria
---

## 1. SendEmailVerificationService Scenarios

### AC-01: 새 이메일로 인증 코드 전송 (REQ-01, REQ-06, REQ-07, REQ-08)

```gherkin
Given 이메일 "test@example.com"에 대한 기존 인증 레코드가 없는 상태에서
When sendVerification("test@example.com")을 호출하면
Then EmailVerificationPort.deleteByEmail("test@example.com")이 호출되어야 한다
And EmailVerificationPort.save()에 전달된 EmailVerification의 code는 "123456"이어야 한다
And verified는 false이어야 한다
And expiredAt은 현재 시간으로부터 약 5분 후이어야 한다
And createdAt은 현재 시간 부근이어야 한다
```

### AC-02: 기존 레코드 존재 시 덮어쓰기 (REQ-01)

```gherkin
Given 이메일 "test@example.com"에 대한 기존 인증 레코드가 존재하는 상태에서
When sendVerification("test@example.com")을 호출하면
Then EmailVerificationPort.deleteByEmail("test@example.com")이 먼저 호출되어야 한다
And 새로운 EmailVerification이 EmailVerificationPort.save()로 저장되어야 한다
```

### AC-03: SMTP 미사용 확인 (REQ-09)

```gherkin
Given 시스템에 SMTP 설정이 없는 상태에서
When sendVerification을 호출하면
Then 외부 이메일 서비스 호출 없이 DB 저장만 수행되어야 한다
And 예외가 발생하지 않아야 한다
```

## 2. VerifyEmailService Scenarios

### AC-04: 올바른 코드로 인증 성공 (REQ-02)

```gherkin
Given 이메일 "test@example.com"에 대해 code="123456", verified=false, expiredAt이 미래인 인증 레코드가 존재하는 상태에서
When verify("test@example.com", "123456")을 호출하면
Then verification.verify()가 호출되어 verified=true로 변경되어야 한다
And EmailVerificationPort.save()에 전달된 엔티티의 verified는 true이어야 한다
```

### AC-05: 존재하지 않는 이메일로 인증 시도 (REQ-03)

```gherkin
Given 이메일 "unknown@example.com"에 대한 인증 레코드가 없는 상태에서
When verify("unknown@example.com", "123456")을 호출하면
Then BusinessException이 발생해야 한다
And 에러 코드는 INVALID_VERIFICATION_CODE (AUTH_008)이어야 한다
```

### AC-06: 만료된 인증 코드로 인증 시도 (REQ-04)

```gherkin
Given 이메일 "test@example.com"에 대해 expiredAt이 과거 시간인 인증 레코드가 존재하는 상태에서
When verify("test@example.com", "123456")을 호출하면
Then BusinessException이 발생해야 한다
And 에러 코드는 INVALID_VERIFICATION_CODE (AUTH_008)이어야 한다
And EmailVerificationPort.save()는 호출되지 않아야 한다
```

### AC-07: 잘못된 인증 코드로 인증 시도 (REQ-05)

```gherkin
Given 이메일 "test@example.com"에 대해 code="123456", expiredAt이 미래인 인증 레코드가 존재하는 상태에서
When verify("test@example.com", "999999")을 호출하면
Then BusinessException이 발생해야 한다
And 에러 코드는 INVALID_VERIFICATION_CODE (AUTH_008)이어야 한다
And EmailVerificationPort.save()는 호출되지 않아야 한다
```

## 3. Quality Gate Criteria

### Test Coverage

| Target Class | Required Coverage |
|-------------|------------------|
| SendEmailVerificationService | 100% line coverage |
| VerifyEmailService | 100% line coverage |

### Code Quality

- [ ] 모든 테스트 통과 (`./gradlew :tmk-core:test`)
- [ ] BusinessException 에러 코드 일관성 검증
- [ ] Mockito verify로 Port 메서드 호출 순서 검증
- [ ] ArgumentCaptor로 저장된 엔티티 필드 값 정확성 검증

## 4. Verification Methods

| Criteria | Method | Tool |
|----------|--------|------|
| 서비스 로직 정확성 | 단위 테스트 | JUnit 5 + Mockito |
| 엔티티 필드 검증 | ArgumentCaptor | Mockito |
| 예외 발생 검증 | assertThrows | JUnit 5 |
| Port 호출 검증 | verify() | Mockito |
| 커버리지 측정 | JaCoCo Report | Gradle JaCoCo |

## 5. Definition of Done

- [ ] SendEmailVerificationService.sendVerification() 구현 완료
- [ ] VerifyEmailService.verify() 구현 완료
- [ ] SendEmailVerificationServiceTest 작성 및 통과
- [ ] VerifyEmailServiceTest 작성 및 통과
- [ ] 모든 AC (AC-01 ~ AC-07) 테스트로 검증 완료
- [ ] 대상 클래스 100% 라인 커버리지 달성
- [ ] `./gradlew :tmk-core:test` 전체 통과
