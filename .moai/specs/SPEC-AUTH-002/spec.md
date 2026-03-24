# SPEC-AUTH-002: 로그인/로그아웃/토큰 재발급

---
spec_id: SPEC-AUTH-002
title: Login, Logout, Token Reissue
created: 2026-03-18
updated: 2026-03-18
status: approved
priority: High
lifecycle: spec-first
assigned: manager-ddd
related_specs: [SPEC-AUTH-001]
---

## 1. Environment

- **Project**: TMK (Test My Knowledge) - AI 기반 문제은행 플랫폼
- **Module**: tmk-core (순수 도메인), tmk-api (Spring Security, JWT, Redis)
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.5.x
- **Database**: PostgreSQL 14+
- **Cache**: Redis (refresh token 저장, access token 블랙리스트)
- **Architecture**: Clean Architecture (Hexagonal) - Port & Adapter 패턴
- **Dependency Direction**: tmk-api -> tmk-core <- tmk-batch (core는 외부 의존 없음)

## 2. Assumptions

- **AS-1**: `PasswordEncoderPort` 인터페이스가 tmk-core에 이미 존재한다 (`matches(rawPassword, encodedPassword)` 메서드 제공)
- **AS-2**: `JwtProvider`가 tmk-api에 이미 존재하며 `generateAccessToken(userId, email, role)`, `generateRefreshToken(userId)`, `validateToken(token)`, `getSubject(token)`, `getExpiration(token)` 메서드를 제공한다
- **AS-3**: `JwtProperties`가 tmk-api에 존재하며 `accessTokenExpiry`, `refreshTokenExpiry` 값을 제공한다
- **AS-4**: `UserRepository` (port/out)에 `findByEmail(email)` 메서드가 이미 존재한다
- **AS-5**: `BusinessException(ErrorCode)` 도메인 예외 패턴이 이미 존재한다
- **AS-6**: `ErrorCode.INVALID_CREDENTIALS` (AUTH_006, 401)와 `ErrorCode.REFRESH_TOKEN_INVALID` (AUTH_009, 401)가 이미 정의되어 있다
- **AS-7**: `JwtAuthenticationFilter`가 tmk-api에 이미 존재하며, JWT 서명 검증 로직이 구현되어 있다
- **AS-8**: Spring Security의 `PasswordEncoder` (BCrypt) 빈이 이미 등록되어 있다
- **AS-9**: `LoginResult`, `ReissueResult` DTO가 tmk-core port/in/auth/dto에 이미 정의되어 있다
- **AS-10**: Redis 연결 설정이 이미 완료되어 있다 (이메일 인증에서 사용 중)

## 3. Requirements

### 3.1 Event-Driven Requirements (WHEN ... THEN ...)

- **REQ-AUTH-006**: WHEN 사용자가 올바른 이메일과 비밀번호로 로그인을 요청하면, THEN 시스템은 JWT access token과 refresh token을 생성하고, refresh token을 Redis에 저장한 후, 두 토큰과 access token 만료 시간을 반환해야 한다.

- **REQ-AUTH-007**: WHEN 사용자가 존재하지 않는 이메일로 로그인을 시도하면, THEN 시스템은 `INVALID_CREDENTIALS` (AUTH_006, 401) 예외를 발생시켜야 한다.

- **REQ-AUTH-008**: WHEN 사용자가 잘못된 비밀번호로 로그인을 시도하면, THEN 시스템은 `INVALID_CREDENTIALS` (AUTH_006, 401) 예외를 발생시켜야 한다.

- **REQ-AUTH-009**: WHEN 인증된 사용자가 로그아웃을 요청하면, THEN 시스템은 해당 access token을 Redis 블랙리스트에 추가하고(남은 만료 시간을 TTL로 설정), 해당 사용자의 refresh token을 Redis에서 삭제해야 한다.

- **REQ-AUTH-010**: WHEN 사용자가 유효한 refresh token으로 토큰 재발급을 요청하면, THEN 시스템은 Redis에 저장된 refresh token과 일치 여부를 확인하고, 새로운 access token을 생성하여 반환해야 한다.

- **REQ-AUTH-011**: WHEN 사용자가 만료되었거나 Redis에 존재하지 않는 refresh token으로 재발급을 요청하면, THEN 시스템은 `REFRESH_TOKEN_INVALID` (AUTH_009, 401) 예외를 발생시켜야 한다.

- **REQ-AUTH-012**: WHEN JwtAuthenticationFilter가 요청의 access token을 검증할 때, THEN 시스템은 JWT 서명 검증 후 Redis 블랙리스트에서 해당 토큰의 존재 여부를 추가로 확인해야 한다. 블랙리스트에 존재하면 인증을 설정하지 않고 필터 체인을 계속 진행해야 한다.

### 3.2 Ubiquitous Requirements (시스템은 항상 ... 해야 한다)

- **REQ-AUTH-013**: 시스템은 항상 refresh token을 Redis에 `refresh_token:{userId}` 키로 저장하고, TTL을 `jwtProperties.getRefreshTokenExpiry()` (밀리초 단위를 초 단위로 변환)로 설정해야 한다.

- **REQ-AUTH-014**: 시스템은 항상 로그아웃 시 access token을 Redis에 `token_blacklist:{accessToken}` 키로 저장하고, TTL을 해당 토큰의 남은 만료 시간(초 단위)으로 설정해야 한다.

### 3.3 Unwanted Requirements (시스템은 ... 하지 않아야 한다)

- **REQ-AUTH-015**: 시스템은 로그인 실패 시 이메일이 존재하지 않는 것인지, 비밀번호가 틀린 것인지 구분하여 응답하지 않아야 한다 (동일한 `INVALID_CREDENTIALS` 에러 반환).

- **REQ-AUTH-016**: tmk-core 모듈의 `LoginService`는 Spring, Redis, JWT 등 외부 프레임워크에 의존하지 않아야 한다 (순수 도메인 로직만 포함).

## 4. Specifications

### 4.1 PasswordEncoderAdapter (tmk-api)

**위치**: `tmk-api/src/main/java/com/tmk/api/adapter/out/security/PasswordEncoderAdapter.java`

**역할**: tmk-core의 `PasswordEncoderPort` 인터페이스 구현체. Spring의 `PasswordEncoder` (BCrypt)에 위임.

**메서드**:
- `matches(String rawPassword, String encodedPassword)`: Spring `PasswordEncoder.matches()`에 위임하여 비밀번호 일치 여부 반환

### 4.2 LoginService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/auth/service/LoginService.java`

**login(String email, String rawPassword) 흐름**:
1. `userPort.findByEmail(email)` 호출 -> 사용자 미존재 시 `BusinessException(ErrorCode.INVALID_CREDENTIALS)` 발생
2. `passwordEncoderPort.matches(rawPassword, user.getPassword())` 호출 -> 불일치 시 `BusinessException(ErrorCode.INVALID_CREDENTIALS)` 발생
3. 검증된 `User` 객체 반환

**의존성**: `UserRepository` (port), `PasswordEncoderPort` (port) - 외부 프레임워크 의존 없음

### 4.3 AuthUseCase.login() (tmk-api)

**위치**: `tmk-api/src/main/java/com/tmk/api/auth/usecase/AuthUseCase.java`

**login(String email, String rawPassword) 흐름**:
1. `loginService.login(email, rawPassword)` 호출 -> 검증된 `User` 반환
2. `jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name())` -> access token 생성
3. `jwtProvider.generateRefreshToken(user.getId())` -> refresh token 생성
4. Redis 저장: key = `refresh_token:{userId}`, value = refreshToken, TTL = `jwtProperties.getRefreshTokenExpiry()` (ms -> s 변환)
5. `new LoginResult(accessToken, refreshToken, jwtProperties.getAccessTokenExpiry())` 반환

### 4.4 AuthUseCase.logout() (tmk-api)

**logout(String accessToken) 흐름**:
1. `jwtProvider.getSubject(accessToken)` -> userId 추출
2. `jwtProvider.getExpiration(accessToken)` -> 만료 시각 추출
3. 남은 만료 시간 계산: `expiration - now` (초 단위)
4. Redis 블랙리스트 등록: key = `token_blacklist:{accessToken}`, value = "true", TTL = 남은 만료 시간(초)
5. Redis refresh token 삭제: key = `refresh_token:{userId}`

### 4.5 ReissueTokenService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/auth/service/ReissueTokenService.java`

**reissue(String refreshToken) 흐름**:
1. refresh token JWT 구문적 유효성 검증 (서명, 만료 등)
2. 유효하면 subject (userId string) 반환
3. 무효하면 `BusinessException(ErrorCode.REFRESH_TOKEN_INVALID)` 발생

**참고**: Redis 확인은 tmk-api의 AuthUseCase에서 수행 (core는 Redis 미의존)

### 4.6 AuthUseCase.reissue() (tmk-api)

**reissue(String refreshToken) 흐름**:
1. `reissueTokenService.reissue(refreshToken)` 호출 -> userId 문자열 반환
2. Redis에서 `refresh_token:{userId}` 키 조회 -> 저장된 토큰이 요청 토큰과 불일치하거나 존재하지 않으면 `BusinessException(ErrorCode.REFRESH_TOKEN_INVALID)` 발생
3. `userPort.findById(userId)` -> 사용자 조회
4. `jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name())` -> 새 access token 생성
5. `new ReissueResult(newAccessToken, jwtProperties.getAccessTokenExpiry())` 반환

### 4.7 JwtAuthenticationFilter 블랙리스트 체크 (tmk-api)

**위치**: `tmk-api/src/main/java/com/tmk/api/security/JwtAuthenticationFilter.java`

**기존 흐름에 추가할 로직**:
1. JWT 서명 검증 통과 후
2. Redis에서 `token_blacklist:{token}` 키 존재 여부 확인
3. 키가 존재하면 -> SecurityContext에 인증 정보를 설정하지 않고 `filterChain.doFilter()` 계속 진행 (401 직접 반환하지 않음)
4. 키가 존재하지 않으면 -> 기존 로직대로 SecurityContext에 인증 정보 설정

## 5. Redis Key Patterns

| Key Pattern | Value | TTL | 용도 |
|---|---|---|---|
| `refresh_token:{userId}` | refresh token 문자열 | 7일 (jwtProperties.refreshTokenExpiry) | 로그인 시 저장, 로그아웃 시 삭제, 재발급 시 검증 |
| `token_blacklist:{accessToken}` | `"true"` | access token 남은 만료 시간 | 로그아웃 시 등록, JwtAuthenticationFilter에서 검증 |

## 6. Traceability

| Requirement | Implementation | Test |
|---|---|---|
| REQ-AUTH-006 | LoginService.login() + AuthUseCase.login() | TC-LOGIN-001 |
| REQ-AUTH-007 | LoginService.login() | TC-LOGIN-002 |
| REQ-AUTH-008 | LoginService.login() | TC-LOGIN-003 |
| REQ-AUTH-009 | AuthUseCase.logout() | TC-LOGOUT-001 |
| REQ-AUTH-010 | ReissueTokenService.reissue() + AuthUseCase.reissue() | TC-REISSUE-001 |
| REQ-AUTH-011 | AuthUseCase.reissue() | TC-REISSUE-002 |
| REQ-AUTH-012 | JwtAuthenticationFilter | TC-FILTER-001 |
| REQ-AUTH-013 | AuthUseCase.login() | TC-LOGIN-001 |
| REQ-AUTH-014 | AuthUseCase.logout() | TC-LOGOUT-001 |
| REQ-AUTH-015 | LoginService.login() | TC-LOGIN-002, TC-LOGIN-003 |
| REQ-AUTH-016 | LoginService (no Spring/Redis imports) | TC-ARCH-001 |
