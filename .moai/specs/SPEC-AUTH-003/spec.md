---
id: SPEC-AUTH-003
version: "1.0.0"
status: approved
created: "2026-03-26"
updated: "2026-03-26"
author: MoAI
priority: HIGH
issue_number: 0
---

# SPEC-AUTH-003: Auth Service Delegation Refactoring

## 1. Overview

`AuthUseCase`가 logout/reissue 로직을 직접 처리하고 있으며, `LogoutService`와 `ReissueTokenService`는 빈 껍데기 상태이다. Port 추상화를 통해 tmk-core 서비스가 실제 로직을 소유하도록 리팩토링한다.

## 2. Problem Statement

| 현상 | 문제 |
|------|------|
| `AuthUseCase`가 `RedisTemplate` 직접 사용 | API 레이어가 인프라 세부사항에 결합 |
| `LogoutService.logout()` 빈 메서드 | 도메인 서비스가 책임을 방기 |
| `ReissueTokenService.reissue()` 빈 메서드 | 도메인 서비스가 책임을 방기 |
| login 시 refresh token Redis 저장을 `AuthUseCase`가 직접 처리 | 인프라 로직과 비즈니스 로직 혼재 |

## 3. Solution

### 3.1 New Port Interfaces (tmk-core)

**RefreshTokenPort** (`port/out/RefreshTokenPort.java`):
- `void save(Long userId, String token, long ttlSeconds)`
- `Optional<String> find(Long userId)`
- `void delete(Long userId)`

**TokenBlacklistPort** (`port/out/TokenBlacklistPort.java`):
- `void blacklist(String token, long ttlSeconds)`

### 3.2 Redis Adapters (tmk-api)

**RedisRefreshTokenAdapter** (`adapter/out/redis/RedisRefreshTokenAdapter.java`):
- `RefreshTokenPort` 구현체
- Key: `"refresh_token:" + userId`

**RedisTokenBlacklistAdapter** (`adapter/out/redis/RedisTokenBlacklistAdapter.java`):
- `TokenBlacklistPort` 구현체
- Key: `"token_blacklist:" + token`

### 3.3 Service Implementation (tmk-core)

**LogoutService** — 시그니처 변경:
```
logout(Long userId, String accessToken, long remainingTtlSeconds)
```
- `TokenBlacklistPort.blacklist(accessToken, remainingTtlSeconds)` — remainingTtlSeconds > 0 시만
- `RefreshTokenPort.delete(userId)`

**ReissueTokenService** — 시그니처 변경:
```
validateRefreshTokenAndGetUser(Long userId, String refreshToken): User
```
- `RefreshTokenPort.find(userId)` → stored token 일치 검증
- `UserPort.findById(userId)` → User 반환

### 3.4 AuthUseCase Updates (tmk-api)

- `login()`: `redisTemplate` 직접 호출 → `refreshTokenPort.save()` 위임
- `logout()`: JWT 파싱 후 → `logoutService.logout()` 위임
- `reissue()`: JWT 파싱/검증 후 → `reissueTokenService.validateRefreshTokenAndGetUser()` 위임
- 의존성: `RedisTemplate`, `UserPort` 제거

## 4. Acceptance Criteria

- [ ] `LogoutService`가 실제 blacklist + refresh token 삭제 로직을 포함
- [ ] `ReissueTokenService`가 실제 refresh token 검증 + 사용자 조회 로직을 포함
- [ ] `AuthUseCase`에서 `RedisTemplate` 의존성 제거
- [ ] `AuthUseCase`에서 `UserPort` 의존성 제거 (reissue에서만 사용하던 것)
- [ ] 기존 동작 동일하게 유지 (login/logout/reissue API 응답 변경 없음)
