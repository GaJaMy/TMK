# Research: TMK 클린 아키텍처 준수 여부 분석

**Date**: 2026-03-12
**Scope**: tmk-core, tmk-api, tmk-batch 전체 코드베이스 심층 분석

---

## Executive Summary

**준수율 점수: 35/100** — 클린 아키텍처 원칙에 심각한 위반 사항 발견.
모듈 의존성 방향은 올바르지만, core 모듈 내에 Spring/JPA 프레임워크 의존성이 광범위하게 존재.

---

## 1. CRITICAL 위반사항

### 1.1 도메인 엔티티에 JPA 어노테이션 직접 적용 (심각도: CRITICAL)

모든 tmk-core 도메인 Aggregate Root와 Entity가 `jakarta.persistence` JPA 어노테이션을 직접 포함:

| 파일 경로 | 위반 내용 | 라인 |
|-----------|-----------|------|
| `tmk-core/.../user/entity/User.java` | `@Entity`, `@Table`, `@Column`, `@GeneratedValue`, `@Enumerated` | 8-41 |
| `tmk-core/.../exam/entity/Exam.java` | `@Entity`, `@OneToMany`, `@Column`, `@GeneratedValue` | 10-50 |
| `tmk-core/.../question/entity/Question.java` | `@Entity`, `@OneToMany`, `@Column` | 10-51 |
| `tmk-core/.../emailverification/entity/EmailVerification.java` | `@Entity`, `@Column`, `@GeneratedValue` | 8-38 |
| `tmk-core/.../exam/entity/ExamQuestion.java` | `@Entity`, `@ManyToOne`, `@JoinColumn` | 6-41 |
| `tmk-core/.../question/entity/QuestionOption.java` | `@Entity`, `@ManyToOne`, `@JoinColumn` | 6-27 |
| `tmk-core/.../document/entity/Document.java` | `@Entity`, `@Table`, `@Column` | 8-36 |
| `tmk-core/.../document/entity/DocumentChunk.java` | `@Entity`, `@Column` (pgvector 포함) | 9-35 |

**문제**: 클린 아키텍처에서 도메인 엔티티는 프레임워크 독립적이어야 함. JPA 어노테이션은 인프라 구현 세부사항으로 adapter 레이어에 속해야 함.

### 1.2 Repository 인터페이스가 Spring Data JpaRepository 상속 (심각도: CRITICAL)

tmk-core의 모든 Repository 인터페이스가 `JpaRepository<T, Long>`를 상속:

| 파일 경로 | 위반 내용 | 라인 |
|-----------|-----------|------|
| `tmk-core/.../user/repository/UserRepository.java` | `extends JpaRepository<User, Long>` | 9 |
| `tmk-core/.../exam/repository/ExamRepository.java` | `extends JpaRepository<Exam, Long>` | 12 |
| `tmk-core/.../question/repository/QuestionRepository.java` | `extends JpaRepository<Question, Long>` | 12 |
| `tmk-core/.../document/repository/DocumentRepository.java` | `extends JpaRepository<Document, Long>` | 6 |
| `tmk-core/.../document/repository/DocumentChunkRepository.java` | `extends JpaRepository<DocumentChunk, Long>` | 8 |
| `tmk-core/.../emailverification/repository/EmailVerificationRepository.java` | `extends JpaRepository<...>` | - |

**문제**: 도메인 포트 인터페이스는 도메인 로직에 필요한 메서드만 정의해야 함. `JpaRepository` 상속 시 `flush()`, `saveAndFlush()`, `deleteInBatch()` 등 인프라 세부사항이 도메인에 노출됨.

### 1.3 도메인 서비스에 @Service 어노테이션 (심각도: HIGH)

tmk-core의 모든 서비스 클래스가 Spring Bean으로 관리됨 (grep 결과: 23개 서비스 클래스에 `@Service` 어노테이션):

- `tmk-core/.../auth/service/LoginService.java` — Line 8: `@Service`
- `tmk-core/.../exam/service/ExamCreationService.java` — Line 19: `@Service`
- `tmk-core/.../document/service/RegisterDocumentService.java` — Line 8: `@Service`
- (외 20개 서비스)

**문제**: 도메인 로직은 Spring 컨텍스트 없이 POJO로 인스턴스화 가능해야 함. `@Service`가 붙은 도메인 서비스는 테스트 시 Spring 컨텍스트가 필요해 순수 단위 테스트 불가.

### 1.4 Core 모듈에 프레임워크 Configuration 클래스 (심각도: HIGH)

| 파일 경로 | 위반 내용 | 라인 |
|-----------|-----------|------|
| `tmk-core/.../redis/RedisConfig.java` | `@Configuration` + `@Bean` RedisTemplate | 9-21 |
| `tmk-core/.../jwt/JwtProperties.java` | `@Component` + `@ConfigurationProperties` | 10-11 |
| `tmk-core/.../jwt/JwtProvider.java` | `@Component` 어노테이션 | 12 |

**문제**: 이 파일들은 인프라 컴포넌트로 tmk-api에 있어야 함. Core에 Redis, JWT 설정이 존재하면 도메인이 특정 기술(Redis, JWT)에 의존하게 됨.

---

## 2. 아키텍처 패턴 위반

### 2.1 port/in UseCase 인터페이스 부재 (심각도: HIGH)

CLAUDE.md 설계에서 정의한 `tmk-core/port/in/` 구조가 실제 코드에 존재하지 않음.

- UseCase 인터페이스가 tmk-**api** 내부 (`tmk-api/.../usecase/AuthUseCase.java` 등)에 위치
- 의존성 역전: core가 api의 인터페이스를 사용하는 구조

**4개 잘못 위치한 UseCase 파일**:
1. `tmk-api/.../auth/usecase/AuthUseCase.java` (`@Component`)
2. `tmk-api/.../document/usecase/DocumentUseCase.java` (`@Component`)
3. `tmk-api/.../question/usecase/QuestionUseCase.java` (`@Component`)
4. `tmk-api/.../exam/usecase/ExamUseCase.java` (`@Component`)

### 2.2 UseCase 구현체 미완성 (심각도: MEDIUM)

tmk-api의 UseCase 클래스들이 TODO로 채워진 스텁 상태:

```java
// AuthUseCase.java Lines 35-39
public LoginResult login(String email, String rawPassword) {
    loginService.login(email, rawPassword);
    // TODO: build LoginResult from user + token
    return null;  // null 반환!
}
```

### 2.3 adapter/out 레이어 부재 (심각도: MEDIUM)

CLAUDE.md에서 정의한 `tmk-api/adapter/out/` 구조가 존재하지 않음. Repository 어댑터 패턴 미구현 상태.

---

## 3. 잘 구현된 부분 (Good Patterns)

### 3.1 모듈 의존성 방향 (EXCELLENT)
- `tmk-api → tmk-core`, `tmk-batch → tmk-core` ✅
- tmk-core는 tmk-api, tmk-batch에 의존하지 않음 ✅

### 3.2 도메인 비즈니스 로직 (GOOD)
`Exam.java` (Lines 51-73): `isExpired()`, `submit()` 등 비즈니스 규칙 구현 ✅

### 3.3 도메인 서비스 로직 분리 (GOOD)
- `ExamCreationService`: 시험 생성 알고리즘 분리 ✅
- `ExamGradingService`: 채점 로직 분리 ✅

### 3.4 Value Objects & Enums (EXCELLENT)
- `Provider`, `UserRole`, `ExamStatus`, `QuestionType`, `Difficulty` 등 타입 안전 열거형 ✅
- `JwtClaims` record 타입 사용 ✅

### 3.5 도메인 예외 처리 (GOOD)
`BusinessException`이 Spring에 의존하지 않는 순수 도메인 예외 ✅

---

## 4. 준수율 스코어카드

| 레이어/모듈 | 점수 | 상태 | 핵심 이슈 |
|------------|------|------|----------|
| tmk-core 도메인 엔티티 | 10/100 | CRITICAL | JPA 어노테이션 전면 적용 |
| tmk-core Repository | 15/100 | CRITICAL | JpaRepository 상속 |
| tmk-core 서비스 | 40/100 | HIGH VIOLATION | 전체 @Service 적용 |
| tmk-core Configuration | 0/100 | CRITICAL | 프레임워크 설정이 core에 위치 |
| tmk-api Controller | 60/100 | ACCEPTABLE | 기본 구조는 적절 |
| tmk-api UseCase | 30/100 | HIGH VIOLATION | 잘못된 레이어 위치, 미완성 |
| tmk-batch Jobs | 70/100 | ACCEPTABLE | 구조 적절, 구현 미완성 |
| 의존성 방향 | 100/100 | EXCELLENT | 모든 모듈 올바른 방향 |
| Value Objects/Enums | 100/100 | EXCELLENT | 올바른 활용 |
| 도메인 로직 분리 | 85/100 | GOOD | ExamCreationService, GradingService 양호 |

---

## 5. 우선순위별 수정 로드맵

### P0 CRITICAL (클린 아키텍처 핵심 위반)
| 우선순위 | 이슈 | 영향 파일 수 | 난이도 |
|---------|------|------------|--------|
| P0-1 | 도메인 엔티티에서 JPA 어노테이션 제거 | 8개 | HIGH |
| P0-2 | Repository 인터페이스 순수 포트로 변환 | 6개 | HIGH |
| P0-3 | JwtProvider, JwtProperties, RedisConfig → tmk-api 이동 | 3개 | MEDIUM |
| P0-4 | 도메인 서비스에서 @Service/@Component 제거 | 23개 | MEDIUM |

### P1 HIGH (아키텍처 원칙 위반)
| 우선순위 | 이슈 | 영향 파일 수 | 난이도 |
|---------|------|------------|--------|
| P1-1 | tmk-core에 port/in UseCase 인터페이스 생성 | 신규 4개+ | MEDIUM |
| P1-2 | UseCase 클래스를 tmk-core로 이동 | 4개 이동 | MEDIUM |
| P1-3 | tmk-api에 adapter/out 레이어 생성 | 신규 6개+ | MEDIUM |
| P1-4 | Repository Adapter 구현체 작성 | 6개 신규 | MEDIUM |

### P2 MEDIUM (개선 사항)
| 우선순위 | 이슈 | 영향 파일 수 | 난이도 |
|---------|------|------------|--------|
| P2-1 | UseCase TODO 완성 (LoginResult 등 반환값 구현) | 4개 | HIGH |
| P2-2 | JPA Entity 분리 (Domain Object vs JPA Entity) | 8개 신규 | HIGH |
| P2-3 | DTO 변환 레이어 구현 | 신규 | MEDIUM |

---

**총 영향 파일**: 37+ 개 기존 파일, 20+ 개 신규 파일 필요
