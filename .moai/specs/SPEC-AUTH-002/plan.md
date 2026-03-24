# SPEC-AUTH-002: Implementation Plan

---
spec_id: SPEC-AUTH-002
type: plan
related_spec: SPEC-AUTH-002
---

## 1. Implementation Order (Priority-Based Milestones)

### Primary Goal: 비밀번호 검증 인프라 (PasswordEncoderAdapter)

**Milestone 1: PasswordEncoderAdapter 구현**
- 파일: `tmk-api/src/main/java/com/tmk/api/adapter/out/security/PasswordEncoderAdapter.java`
- tmk-core의 `PasswordEncoderPort` 인터페이스를 구현
- Spring `PasswordEncoder` (BCrypt) 빈을 주입받아 `matches()` 메서드 위임
- `@Component` 어노테이션으로 Spring Bean 등록
- **의존성**: 없음 (독립적으로 구현 가능)

### Primary Goal: 로그인 기능 구현

**Milestone 2: LoginService (tmk-core)**
- 파일: `tmk-core/src/main/java/com/tmk/core/auth/service/LoginService.java`
- `UserRepository`, `PasswordEncoderPort`를 생성자 주입
- 이메일로 사용자 조회 -> 비밀번호 검증 -> User 반환
- 실패 시 `BusinessException(ErrorCode.INVALID_CREDENTIALS)` 발생
- **의존성**: Milestone 1 (PasswordEncoderAdapter)
- **참고 패턴**: `SendEmailVerificationService`, `VerifyEmailService`

**Milestone 3: AuthUseCase.login() (tmk-api)**
- 파일: `tmk-api/src/main/java/com/tmk/api/auth/usecase/AuthUseCase.java`
- `LoginService`, `JwtProvider`, `JwtProperties`, `RedisTemplate` 주입
- 로그인 서비스 호출 -> JWT 생성 -> Redis refresh token 저장 -> LoginResult 반환
- **의존성**: Milestone 2 (LoginService)

### Secondary Goal: 로그아웃 기능 구현

**Milestone 4: AuthUseCase.logout()**
- 파일: `tmk-api/src/main/java/com/tmk/api/auth/usecase/AuthUseCase.java` (기존 파일에 메서드 추가)
- access token 파싱 -> 블랙리스트 Redis 등록 -> refresh token Redis 삭제
- **의존성**: Milestone 3 (AuthUseCase 클래스 존재)

### Secondary Goal: 토큰 재발급 기능 구현

**Milestone 5: ReissueTokenService (tmk-core)**
- 파일: `tmk-core/src/main/java/com/tmk/core/auth/service/ReissueTokenService.java`
- refresh token JWT 구문적 유효성 검증 (minimal - Redis 확인은 tmk-api에서)
- 유효하면 userId 반환, 무효하면 `REFRESH_TOKEN_INVALID` 예외
- **의존성**: 없음 (독립적으로 구현 가능, Milestone 2와 병렬 가능)

**Milestone 6: AuthUseCase.reissue()**
- 파일: `tmk-api/src/main/java/com/tmk/api/auth/usecase/AuthUseCase.java` (기존 파일에 메서드 추가)
- ReissueTokenService 호출 -> Redis refresh token 검증 -> 새 access token 생성
- **의존성**: Milestone 5 (ReissueTokenService)

### Final Goal: 보안 강화

**Milestone 7: JwtAuthenticationFilter 블랙리스트 체크**
- 파일: `tmk-api/src/main/java/com/tmk/api/security/JwtAuthenticationFilter.java` (기존 파일 수정)
- JWT 서명 검증 통과 후 Redis 블랙리스트 조회 추가
- 블랙리스트에 존재하면 인증 미설정
- **의존성**: Milestone 4 (logout 로직과 연계)

## 2. Redis Key Pattern Definition

| Key | Type | TTL | 설명 |
|-----|------|-----|------|
| `refresh_token:{userId}` | String | 7일 | refresh token 값 저장 |
| `token_blacklist:{accessToken}` | String | access token 남은 만료 시간 | 로그아웃된 토큰 무효화 |

## 3. Dependency Direction

```
tmk-core (순수 도메인)
├── LoginService
│   ├── UserRepository (port/out) - 인터페이스
│   └── PasswordEncoderPort (port/out) - 인터페이스
└── ReissueTokenService
    └── JWT 구문 검증만 (외부 프레임워크 미의존)

tmk-api (Spring, Redis, JWT)
├── PasswordEncoderAdapter -> implements PasswordEncoderPort
├── AuthUseCase
│   ├── LoginService (tmk-core)
│   ├── ReissueTokenService (tmk-core)
│   ├── JwtProvider (tmk-api)
│   ├── JwtProperties (tmk-api)
│   └── RedisTemplate (Spring Data Redis)
└── JwtAuthenticationFilter
    ├── JwtProvider (tmk-api)
    └── RedisTemplate (Spring Data Redis)
```

**핵심 원칙**: tmk-core는 Spring, Redis, JWT 라이브러리에 직접 의존하지 않는다. 외부 기술에 대한 접근은 port 인터페이스를 통해서만 이루어진다.

## 4. Test Strategy

### Unit Tests (tmk-core)

| Test Class | Target | 검증 항목 |
|---|---|---|
| `LoginServiceTest` | `LoginService.login()` | 정상 로그인, 이메일 미존재, 비밀번호 불일치 |
| `ReissueTokenServiceTest` | `ReissueTokenService.reissue()` | 유효한 토큰, 만료된 토큰, 잘못된 토큰 |

### Integration Tests (tmk-api)

| Test Class | Target | 검증 항목 |
|---|---|---|
| `AuthUseCaseTest` | `AuthUseCase` | login/logout/reissue 전체 흐름, Redis 연동 |
| `JwtAuthenticationFilterTest` | `JwtAuthenticationFilter` | 블랙리스트 토큰 거부, 정상 토큰 통과 |
| `PasswordEncoderAdapterTest` | `PasswordEncoderAdapter` | BCrypt 일치/불일치 |

### Architecture Test

| Test | 검증 항목 |
|---|---|
| `ArchitectureTest` | tmk-core 모듈이 Spring/Redis/JWT 패키지를 import하지 않는 것 검증 |

## 5. Risk and Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| Redis 연결 실패 시 로그인/로그아웃 불가 | High | Redis health check, fallback 전략 검토 (Optional Goal) |
| Access token 블랙리스트 Redis 메모리 증가 | Medium | TTL 기반 자동 만료로 메모리 관리 |
| ReissueTokenService의 JWT 검증 범위 | Low | tmk-core에서는 구문적 검증만, Redis 검증은 tmk-api에서 수행 |
| 동시 로그아웃 요청 시 race condition | Low | Redis 단일 키 덮어쓰기로 자연 해결 |

## 6. Expert Consultation Recommendations

- **expert-backend**: API 엔드포인트 설계, Spring Security 필터 체인 구성, Redis 연동 패턴 검토 권장
- **expert-security**: JWT 토큰 블랙리스트 전략, 토큰 탈취 시나리오, OWASP 인증 가이드라인 준수 여부 검토 권장
