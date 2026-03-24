# SPEC-AUTH-002: Acceptance Criteria

---
spec_id: SPEC-AUTH-002
type: acceptance
related_spec: SPEC-AUTH-002
---

## 1. Login Scenarios

### TC-LOGIN-001: 정상 로그인 성공

```gherkin
Feature: 사용자 로그인

Scenario: 올바른 이메일과 비밀번호로 로그인
  Given 이메일 "user@example.com"으로 가입된 사용자가 존재한다
  And 비밀번호는 BCrypt로 암호화되어 저장되어 있다
  When 사용자가 이메일 "user@example.com"과 올바른 비밀번호로 로그인을 요청한다
  Then 응답 상태 코드는 200이다
  And 응답에 accessToken이 포함되어 있다
  And 응답에 refreshToken이 포함되어 있다
  And 응답에 accessTokenExpiry가 포함되어 있다
  And Redis에 "refresh_token:{userId}" 키로 refreshToken이 저장되어 있다
  And Redis의 해당 키 TTL이 7일 이내이다
```

### TC-LOGIN-002: 존재하지 않는 이메일로 로그인 실패

```gherkin
Scenario: 존재하지 않는 이메일로 로그인 시도
  Given 이메일 "nonexistent@example.com"으로 가입된 사용자가 존재하지 않는다
  When 사용자가 이메일 "nonexistent@example.com"과 임의의 비밀번호로 로그인을 요청한다
  Then 응답 상태 코드는 401이다
  And 에러 코드는 "AUTH_006"이다
  And 에러 메시지에 이메일 존재 여부에 대한 힌트가 포함되지 않는다
```

### TC-LOGIN-003: 잘못된 비밀번호로 로그인 실패

```gherkin
Scenario: 잘못된 비밀번호로 로그인 시도
  Given 이메일 "user@example.com"으로 가입된 사용자가 존재한다
  When 사용자가 이메일 "user@example.com"과 잘못된 비밀번호로 로그인을 요청한다
  Then 응답 상태 코드는 401이다
  And 에러 코드는 "AUTH_006"이다
  And 에러 메시지가 TC-LOGIN-002의 에러 메시지와 동일하다
```

## 2. Logout Scenarios

### TC-LOGOUT-001: 정상 로그아웃

```gherkin
Feature: 사용자 로그아웃

Scenario: 인증된 사용자가 로그아웃
  Given 사용자가 유효한 accessToken으로 인증되어 있다
  And 해당 accessToken의 남은 만료 시간이 30분이다
  When 사용자가 로그아웃을 요청한다
  Then 응답 상태 코드는 200이다
  And Redis에 "token_blacklist:{accessToken}" 키가 생성되어 있다
  And 해당 키의 값은 "true"이다
  And 해당 키의 TTL은 약 30분(1800초) 이내이다
  And Redis에서 "refresh_token:{userId}" 키가 삭제되어 있다
```

### TC-LOGOUT-002: 로그아웃 후 access token 재사용 거부

```gherkin
Scenario: 로그아웃된 access token으로 API 접근 시도
  Given 사용자가 로그아웃하여 accessToken이 블랙리스트에 등록되어 있다
  When 해당 accessToken으로 인증이 필요한 API를 요청한다
  Then 응답 상태 코드는 401이다
  And SecurityContext에 인증 정보가 설정되지 않는다
```

## 3. Token Reissue Scenarios

### TC-REISSUE-001: 정상 토큰 재발급

```gherkin
Feature: 토큰 재발급

Scenario: 유효한 refresh token으로 access token 재발급
  Given 사용자가 로그인하여 유효한 refreshToken을 가지고 있다
  And Redis에 "refresh_token:{userId}" 키로 해당 refreshToken이 저장되어 있다
  When 사용자가 해당 refreshToken으로 토큰 재발급을 요청한다
  Then 응답 상태 코드는 200이다
  And 응답에 새로운 accessToken이 포함되어 있다
  And 응답에 accessTokenExpiry가 포함되어 있다
```

### TC-REISSUE-002: 만료된 refresh token으로 재발급 실패

```gherkin
Scenario: 만료된 refresh token으로 재발급 시도
  Given 사용자의 refreshToken이 만료되었다
  When 사용자가 해당 refreshToken으로 토큰 재발급을 요청한다
  Then 응답 상태 코드는 401이다
  And 에러 코드는 "AUTH_009"이다
```

### TC-REISSUE-003: Redis에 존재하지 않는 refresh token으로 재발급 실패

```gherkin
Scenario: Redis에 없는 refresh token으로 재발급 시도 (로그아웃 후 재발급 시도)
  Given 사용자가 로그아웃하여 Redis에서 refresh token이 삭제되었다
  And 사용자의 refreshToken JWT 자체는 아직 만료되지 않았다
  When 사용자가 해당 refreshToken으로 토큰 재발급을 요청한다
  Then 응답 상태 코드는 401이다
  And 에러 코드는 "AUTH_009"이다
```

### TC-REISSUE-004: 변조된 refresh token으로 재발급 실패

```gherkin
Scenario: 변조된 refresh token으로 재발급 시도
  Given 사용자가 JWT 서명이 변조된 refreshToken을 사용한다
  When 사용자가 해당 refreshToken으로 토큰 재발급을 요청한다
  Then 응답 상태 코드는 401이다
  And 에러 코드는 "AUTH_009"이다
```

## 4. JwtAuthenticationFilter Scenarios

### TC-FILTER-001: 블랙리스트 토큰 필터링

```gherkin
Feature: JWT 인증 필터 블랙리스트 검증

Scenario: 블랙리스트에 등록된 토큰으로 요청
  Given access token "eyJhbGciOiJI..."이 블랙리스트에 등록되어 있다
  And 해당 토큰의 JWT 서명은 유효하다
  When 해당 토큰이 Authorization 헤더에 포함된 요청이 들어온다
  Then JwtAuthenticationFilter는 JWT 서명 검증을 통과한다
  And Redis에서 "token_blacklist:{token}" 키를 조회한다
  And 키가 존재하므로 SecurityContext에 인증 정보를 설정하지 않는다
  And filterChain.doFilter()를 호출하여 다음 필터로 진행한다
```

### TC-FILTER-002: 정상 토큰 필터 통과

```gherkin
Scenario: 유효하고 블랙리스트에 없는 토큰으로 요청
  Given access token이 유효하고 블랙리스트에 등록되어 있지 않다
  When 해당 토큰이 Authorization 헤더에 포함된 요청이 들어온다
  Then JwtAuthenticationFilter는 JWT 서명 검증을 통과한다
  And Redis에서 "token_blacklist:{token}" 키를 조회한다
  And 키가 존재하지 않으므로 SecurityContext에 인증 정보를 설정한다
```

## 5. Architecture Validation

### TC-ARCH-001: tmk-core 모듈 순수성 검증

```gherkin
Feature: 클린 아키텍처 준수

Scenario: tmk-core 모듈이 외부 프레임워크에 의존하지 않음
  Given tmk-core 모듈의 LoginService 클래스가 존재한다
  And tmk-core 모듈의 ReissueTokenService 클래스가 존재한다
  When 해당 클래스들의 import 문을 분석한다
  Then "org.springframework" 패키지를 import하지 않는다
  And "redis" 관련 패키지를 import하지 않는다
  And "io.jsonwebtoken" 패키지를 직접 import하지 않는다
  And 모든 외부 의존은 tmk-core의 port 인터페이스를 통해서만 이루어진다
```

## 6. Quality Gates

### Definition of Done

- [ ] 모든 Milestone (1-7) 구현 완료
- [ ] 모든 TC (TC-LOGIN-001 ~ TC-ARCH-001) 통과
- [ ] 단위 테스트 커버리지 85% 이상
- [ ] tmk-core 모듈에 Spring/Redis 의존성 없음 확인
- [ ] Redis 키 패턴이 spec.md에 정의된 것과 일치
- [ ] 로그인 실패 시 이메일/비밀번호 구분 불가 확인 (REQ-AUTH-015)
- [ ] 기존 JwtAuthenticationFilter 동작에 영향 없음 확인
