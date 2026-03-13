---
title: TMK 프로젝트 구조
description: 멀티 모듈 Gradle 프로젝트의 디렉토리 구조 및 패키지 설계
---

# TMK 프로젝트 구조

## 프로젝트 개요

TMK는 클린 아키텍처 기반 멀티 모듈 구조로 설계된 Java 21 + Spring Boot 3.5.11 프로젝트입니다.

**의존성 방향**: `tmk-api` → `tmk-core` ← `tmk-batch` (core는 외부에 의존하지 않음)

---

## 최상위 디렉토리 구조

```
tmk/                           # 프로젝트 루트
├── build.gradle               # 전체 빌드 설정 (멀티 모듈 관리)
├── settings.gradle            # Gradle 모듈 정의
├── README.md                  # 프로젝트 개요
├── CLAUDE.md                  # Claude Code 프로젝트 가이드
│
├── .gradle/                   # Gradle 캐시 (gitignore)
├── .idea/                     # IntelliJ IDEA 설정 (gitignore)
├── build/                     # 빌드 산출물 (gitignore)
├── gradle/                    # Gradle wrapper 바이너리
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── tmk-core/                  # 도메인 모듈
├── tmk-api/                   # REST API 모듈
├── tmk-batch/                 # Spring Batch 모듈
│
├── .claude/                   # Claude Code 설정
│   ├── agents/                # 에이전트 정의
│   ├── skills/                # 스킬 정의
│   ├── commands/              # 커스텀 명령어
│   ├── rules/                 # 프로젝트 규칙
│   ├── hooks/                 # 이벤트 훅
│   └── settings.json          # Claude Code 설정
│
├── .moai/                     # MoAI ADK 설정
│   ├── project/               # 프로젝트 문서
│   │   ├── product.md         # 제품 사양
│   │   ├── structure.md       # 이 파일
│   │   └── tech.md            # 기술 스택
│   ├── specs/                 # SPEC 문서
│   ├── docs/                  # 프로젝트 문서
│   └── state/                 # 상태 저장소
│
├── docs/                      # 공개 문서
│   ├── API 명세서.md
│   ├── 도메인 모델 설계.md
│   ├── ERD 설계.md
│   └── ddl.sql
│
└── .git/                      # Git 저장소 (gitignore)
```

---

## 모듈 구조 상세 설명

### 모듈 1: tmk-core (도메인 모듈)

**역할**: 비즈니스 로직 및 도메인 모델 정의. Spring/JPA 등 외부 프레임워크에 의존하지 않음.

**특징**:
- 순수 Java 도메인 로직
- 사업 규칙을 코드로 표현
- tmk-api와 tmk-batch 양쪽에서 참조

**디렉토리 구조**:

```
tmk-core/
├── src/main/java/com/tmk/core/
│   ├── domain/                        # 도메인 엔티티 및 가치 객체
│   │   ├── user/
│   │   │   ├── User.java              # Aggregate Root: 사용자 정보
│   │   │   ├── UserProvider.java      # Enum: LOCAL, GOOGLE, KAKAO, NAVER
│   │   │   └── UserRole.java          # Enum: ADMIN, USER
│   │   │
│   │   ├── emailverification/
│   │   │   ├── EmailVerification.java # Aggregate Root: 이메일 인증
│   │   │   └── VerificationCode.java  # Value Object: 인증 코드
│   │   │
│   │   ├── document/
│   │   │   ├── Document.java          # Aggregate Root: 학습 자료
│   │   │   ├── DocumentChunk.java     # Entity: 청킹된 텍스트
│   │   │   ├── DocumentStatus.java    # Enum: REGISTERED, PROCESSING, COMPLETED, FAILED
│   │   │   └── DocumentEmbedding.java # Value Object: 벡터 데이터
│   │   │
│   │   ├── question/
│   │   │   ├── Question.java          # Aggregate Root: 시험 문제
│   │   │   ├── QuestionType.java      # Enum: MULTIPLE_CHOICE, SHORT_ANSWER, TRUE_FALSE
│   │   │   ├── QuestionDifficulty.java # Enum: EASY, MEDIUM, HARD
│   │   │   └── Option.java            # Value Object: 객관식 보기
│   │   │
│   │   ├── exam/
│   │   │   ├── Exam.java              # Aggregate Root: 시험 세션
│   │   │   ├── ExamQuestion.java      # Entity: 시험에 포함된 문제
│   │   │   ├── ExamAnswer.java        # Entity: 응시자의 답안
│   │   │   ├── ExamStatus.java        # Enum: IN_PROGRESS, SUBMITTED, GRADED
│   │   │   ├── ExamCreationService.java # Domain Service: 시험 생성 로직
│   │   │   └── ExamGradingService.java  # Domain Service: 채점 로직
│   │   │
│   │   └── shared/
│   │       ├── BaseEntity.java        # 공통 엔티티 기본 클래스
│   │       └── BusinessException.java # 도메인 예외
│   │
│   ├── port/
│   │   ├── in/                        # 인바운드 포트 (UseCase 인터페이스)
│   │   │   ├── auth/
│   │   │   │   ├── SendEmailVerificationUseCase.java
│   │   │   │   ├── VerifyEmailUseCase.java
│   │   │   │   ├── RegisterUseCase.java
│   │   │   │   ├── LoginUseCase.java
│   │   │   │   ├── LogoutUseCase.java
│   │   │   │   ├── SocialLoginUseCase.java
│   │   │   │   ├── ReissueTokenUseCase.java
│   │   │   │   └── dto/
│   │   │   │       ├── LoginResult.java
│   │   │   │       ├── ReissueResult.java
│   │   │   │       └── SocialLoginResult.java
│   │   │   │
│   │   │   ├── document/
│   │   │   │   ├── RegisterDocumentUseCase.java
│   │   │   │   ├── GetDocumentStatusUseCase.java
│   │   │   │   └── dto/
│   │   │   │       ├── RegisterDocumentResult.java
│   │   │   │       └── DocumentStatusResult.java
│   │   │   │
│   │   │   ├── question/
│   │   │   │   ├── GetQuestionListUseCase.java
│   │   │   │   ├── GetQuestionDetailUseCase.java
│   │   │   │   └── dto/
│   │   │   │       ├── OptionResult.java
│   │   │   │       ├── QuestionDetailResult.java
│   │   │   │       ├── QuestionSummary.java
│   │   │   │       └── QuestionListResult.java
│   │   │   │
│   │   │   └── exam/
│   │   │       ├── CreateExamUseCase.java
│   │   │       ├── GetExamUseCase.java
│   │   │       ├── SaveAnswerUseCase.java
│   │   │       ├── SubmitExamUseCase.java
│   │   │       ├── GetExamResultUseCase.java
│   │   │       ├── GetExamHistoryUseCase.java
│   │   │       └── dto/
│   │   │           ├── ExamResult.java
│   │   │           ├── ExamDetailResult.java
│   │   │           ├── AnswerCommand.java
│   │   │           └── SubmitResult.java
│   │   │
│   │   └── out/                       # 아웃바운드 포트 (Repository 인터페이스)
│   │       ├── UserRepository.java
│   │       ├── EmailVerificationRepository.java
│   │       ├── DocumentRepository.java
│   │       ├── DocumentChunkRepository.java
│   │       ├── QuestionRepository.java
│   │       └── ExamRepository.java
│   │
│   └── config/
│       ├── JwtConfig.java             # JWT 설정
│       └── RedisConfig.java           # Redis 설정
│
├── src/main/resources/
│   └── application-core.yml           # 도메인 레이어 설정
│
└── build.gradle                       # tmk-core 빌드 설정
```

**주요 패키지별 책임**:

| 패키지 | 책임 | 예시 |
|--------|------|------|
| `domain.*` | 비즈니스 규칙 구현 | `Exam.submit()`, `ExamQuestion.grade()` |
| `port.in.*` | UseCase 인터페이스 정의 | `CreateExamUseCase` |
| `port.out` | 저장소 추상화 | `UserRepository` 인터페이스 |
| `config` | 공유 설정 (JWT, Redis) | 도메인에서 직접 사용 |

---

### 모듈 2: tmk-api (REST API 모듈)

**역할**: HTTP 요청 처리, Spring Security 인증, JPA 구현체, 비즈니스 로직 조정.

**의존성**: tmk-core → Spring Boot, Spring Security, Spring Data JPA

**디렉토리 구조**:

```
tmk-api/
├── src/main/java/com/tmk/api/
│   ├── controller/                    # HTTP 컨트롤러 (Adapter In)
│   │   ├── AuthController.java        # POST /api/v1/auth/*
│   │   ├── QuestionController.java    # GET /api/v1/questions
│   │   ├── ExamController.java        # POST/GET /api/v1/exams
│   │   └── DocumentController.java    # POST /internal/v1/documents (내부 API)
│   │
│   ├── security/                      # Spring Security 설정
│   │   ├── SecurityConfig.java        # SecurityFilterChain 설정
│   │   ├── JwtAuthenticationFilter.java # JWT 인증 필터
│   │   ├── JwtProvider.java           # JWT 토큰 생성/검증
│   │   ├── CustomUserDetailsService.java # UserDetails 구현
│   │   └── AuthenticationEntryPoint.java # 인증 실패 처리
│   │
│   ├── common/                        # 공통 코드
│   │   ├── ApiResponse.java           # 공통 응답 래퍼
│   │   ├── ErrorCode.java             # 에러 코드 정의
│   │   └── GlobalExceptionHandler.java # 전역 예외 처리
│   │
│   ├── adapter/                       # Hexagonal Architecture 구현체
│   │   ├── in/                        # 인바운드 어댑터
│   │   │   └── web/                   # 컨트롤러가 직접 참조하는 파사드
│   │   │       └── *Service.java      # UseCase 구현 (컨트롤러 ← 파사드 ← UseCase)
│   │   │
│   │   └── out/                       # 아웃바운드 어댑터 (JPA 구현체)
│   │       ├── persistence/           # JPA Entity
│   │       │   ├── UserJpaEntity.java
│   │       │   ├── DocumentJpaEntity.java
│   │       │   ├── QuestionJpaEntity.java
│   │       │   └── ExamJpaEntity.java
│   │       │
│   │       ├── repository/            # JPA Repository 인터페이스
│   │       │   ├── UserJpaRepository.java (Spring Data JPA)
│   │       │   ├── DocumentJpaRepository.java
│   │       │   └── *Repository.java
│   │       │
│   │       └── adapter/               # Adapter 구현체 (Repository 인터페이스 구현)
│   │           ├── UserRepositoryAdapter.java
│   │           ├── DocumentRepositoryAdapter.java
│   │           └── *RepositoryAdapter.java
│   │
│   ├── config/                        # Spring 설정
│   │   ├── WebConfig.java             # CORS, 컨버터 설정
│   │   ├── JpaAuditingConfig.java     # 감시(created, modified) 설정
│   │   └── RedisConfig.java           # Redis 클라이언트 설정
│   │
│   └── TmkApiApplication.java         # Spring Boot 메인 클래스
│
├── src/main/resources/
│   ├── application.yml                # 기본 설정
│   ├── application-local.yml          # 로컬 개발 환경
│   ├── application-dev.yml            # 개발 환경
│   └── application-prod.yml           # 프로덕션 환경
│
└── build.gradle                       # tmk-api 빌드 설정
```

**아키텍처 흐름**:

```
HTTP Request
    ↓
Controller (@RestController)
    ↓
Service Facade (UseCase 구현, tmk-api에서 정의)
    ↓
Domain UseCase (tmk-core에서 인터페이스 정의)
    ↓
Entity (도메인 로직 실행)
    ↓
RepositoryAdapter (interface 구현체, tmk-api에서 정의)
    ↓
JpaRepository (Spring Data JPA)
    ↓
PostgreSQL DB
    ↓
... (응답 역방향)
```

**주요 패키지별 책임**:

| 패키지 | 책임 | 예시 |
|--------|------|------|
| `controller` | HTTP 요청/응답 처리 | `@PostMapping("/login")` |
| `security` | 인증, 인가, JWT 관리 | `JwtProvider`, `SecurityConfig` |
| `adapter.in` | UseCase 구현 (컨트롤러용) | `CreateExamServiceImpl` |
| `adapter.out` | Repository 구현체 | `UserRepositoryAdapter` |
| `common` | 공통 응답, 에러 처리 | `ApiResponse<T>`, `GlobalExceptionHandler` |

---

### 모듈 3: tmk-batch (Spring Batch 모듈)

**역할**: 배치 작업 정의 및 실행 (자동 제출, 정리 작업 등).

**의존성**: tmk-core → Spring Batch

**디렉토리 구조**:

```
tmk-batch/
├── src/main/java/com/tmk/batch/
│   ├── config/                        # Batch 설정
│   │   ├── BatchConfig.java           # @EnableBatchProcessing, JobRepository 설정
│   │   └── DataSourceConfig.java      # Batch 메타데이터 DB 설정
│   │
│   ├── job/                           # 배치 작업
│   │   ├── ExamAutoSubmitJob.java     # 매분 만료된 시험 자동 제출 및 채점
│   │   │   ├── ExamAutoSubmitStep.java
│   │   │   └── ExamAutoSubmitProcessor.java
│   │   │
│   │   └── ExpiredVerificationCleanJob.java # 매일 새벽 만료 인증코드 삭제
│   │       ├── ExpiredVerificationCleanStep.java
│   │       └── ExpiredVerificationCleanProcessor.java
│   │
│   ├── reader/                        # 데이터 읽기
│   │   ├── ExpiredExamReader.java     # 만료된 시험 조회
│   │   └── ExpiredVerificationReader.java # 만료된 인증 코드 조회
│   │
│   ├── processor/                     # 데이터 처리
│   │   ├── ExamSubmitProcessor.java   # 시험 제출 처리
│   │   └── VerificationDeleteProcessor.java # 인증 코드 삭제 처리
│   │
│   ├── writer/                        # 데이터 기록
│   │   ├── ExamGradeWriter.java       # 채점 결과 저장
│   │   └── VerificationDeleteWriter.java # 삭제 처리 기록
│   │
│   ├── schedule/                      # 배치 스케줄러
│   │   └── BatchScheduler.java        # 주기적 배치 실행 설정
│   │
│   └── TmkBatchApplication.java       # Spring Boot 메인 클래스
│
├── src/main/resources/
│   └── application.yml                # 배치 설정
│
└── build.gradle                       # tmk-batch 빌드 설정
```

**주요 배치 작업**:

| 작업명 | 주기 | 역할 | 상태 |
|--------|------|------|------|
| ExamAutoSubmitJob | 매분 | 시간 초과 시험 자동 제출 및 채점 | ⚠️ 구조만 완성 |
| ExpiredVerificationCleanJob | 매일 새벽 2시 | 5분 이상 경과한 인증 코드 삭제 | ⚠️ 구조만 완성 |

**아키텍처 흐름**:

```
Quartz/Spring Scheduler (주기적 실행)
    ↓
Job (배치 작업 정의)
    ↓
Step (작업 단계)
    ↓
Reader (데이터 조회) → Processor (처리) → Writer (저장)
    ↓
Domain UseCase / Repository (도메인 로직 실행)
    ↓
PostgreSQL DB, Redis
```

---

## 패키지 명명 규칙

### Java 패키지 명명

```
com.tmk.core          # 도메인 코어
├── domain            # 도메인 엔티티
│   ├── user
│   ├── document
│   ├── question
│   └── exam
├── port              # 포트 (인터페이스)
│   ├── in            # 인바운드 포트 (UseCase)
│   └── out           # 아웃바운드 포트 (Repository)
└── config            # 설정

com.tmk.api           # REST API 모듈
├── controller        # HTTP 컨트롤러
├── security          # Spring Security
├── common            # 공통 코드
├── adapter           # 헥사고날 어댑터
│   ├── in            # 인바운드 (컨트롤러 구현체)
│   └── out           # 아웃바운드 (JPA 어댑터)
└── config            # Spring 설정

com.tmk.batch         # Spring Batch 모듈
├── config            # Batch 설정
├── job               # 배치 작업
├── reader            # ItemReader
├── processor         # ItemProcessor
├── writer            # ItemWriter
└── schedule          # 스케줄러
```

### 클래스 명명

| 용도 | 패턴 | 예시 |
|------|------|------|
| Entity | `*Entity.java` | `UserEntity`, `ExamEntity` |
| Domain Aggregate | `*.java` | `User`, `Exam` |
| Value Object | `*.java` | `VerificationCode`, `Option` |
| Service | `*Service.java` | `ExamCreationService`, `ExamGradingService` |
| UseCase 인터페이스 | `*UseCase.java` | `LoginUseCase`, `CreateExamUseCase` |
| UseCase 구현 | `*ServiceImpl.java` | `LoginServiceImpl` |
| Controller | `*Controller.java` | `AuthController`, `ExamController` |
| Repository 인터페이스 | `*Repository.java` | `UserRepository`, `ExamRepository` |
| JPA Repository | `*JpaRepository.java` | `UserJpaRepository`, `ExamJpaRepository` |
| Repository 어댑터 | `*RepositoryAdapter.java` | `UserRepositoryAdapter` |
| Config | `*Config.java` | `SecurityConfig`, `JpaAuditingConfig` |
| DTO | `*Result.java`, `*Command.java` | `LoginResult`, `AnswerCommand` |
| Enum | `*.java` (PascalCase) | `UserProvider`, `QuestionType` |

---

## 의존성 아키텍처

### 빌드 시점 의존성

```
tmk-api (REST API)
  └─→ tmk-core (도메인)
      └─→ [Spring/JPA 런타임 의존만 가능]

tmk-batch (배치)
  └─→ tmk-core (도메인)
      └─→ [Spring 런타임 의존만 가능]

tmk-core (도메인)
  └─→ [외부 프레임워크 NO]
      └─→ lombok, jakarta.persistence만 허용
```

### 런타임 의존성

```
HTTP Request
  ↓
tmk-api (Spring Boot)
  ├─→ Spring Security (JWT 인증)
  ├─→ Spring Data JPA (ORM)
  ├─→ PostgreSQL Driver (DB)
  ├─→ Redis Client (캐시)
  └─→ tmk-core (도메인 로직)
      ├─→ ExamCreationService (시험 생성)
      └─→ ExamGradingService (채점)

Batch Scheduler
  ↓
tmk-batch (Spring Boot)
  ├─→ Spring Batch (배치 프레임워크)
  ├─→ Quartz (스케줄링)
  ├─→ PostgreSQL Driver (DB)
  └─→ tmk-core (도메인 로직)
```

---

## 모듈별 책임 매트릭스

| 기능 영역 | tmk-core | tmk-api | tmk-batch |
|----------|----------|---------|-----------|
| **도메인 로직** | ✅ | ❌ | ❌ |
| **비즈니스 규칙** | ✅ | 적용만 | 적용만 |
| **Entity 정의** | ✅ | JPA 매핑만 | ❌ |
| **UseCase 정의** | ✅ (인터페이스) | ✅ (구현) | ❌ |
| **Repository 인터페이스** | ✅ | 구현만 | ❌ |
| **HTTP 요청 처리** | ❌ | ✅ | ❌ |
| **JWT 인증** | 설정만 | ✅ (적용) | 설정만 |
| **데이터베이스 접근** | ❌ | ✅ | ❌ |
| **배치 스케줄링** | ❌ | ❌ | ✅ |
| **외부 API 호출** | ❌ | ✅ (OpenAI, 소셜) | ❌ |

---

## 주요 파일 위치 참조표

### 도메인 관련

| 항목 | 경로 |
|------|------|
| User Aggregate | `tmk-core/src/main/java/.../domain/user/User.java` |
| Exam Aggregate | `tmk-core/src/main/java/.../domain/exam/Exam.java` |
| 채점 로직 | `tmk-core/src/main/java/.../domain/exam/ExamGradingService.java` |
| 시험 생성 로직 | `tmk-core/src/main/java/.../domain/exam/ExamCreationService.java` |

### API 관련

| 항목 | 경로 |
|------|------|
| 인증 컨트롤러 | `tmk-api/src/main/java/.../controller/AuthController.java` |
| JWT 설정 | `tmk-api/src/main/java/.../security/JwtProvider.java` |
| 공통 응답 | `tmk-api/src/main/java/.../common/ApiResponse.java` |
| 에러 처리 | `tmk-api/src/main/java/.../common/GlobalExceptionHandler.java` |
| JPA Entity | `tmk-api/src/main/java/.../adapter/out/persistence/*.java` |

### 배치 관련

| 항목 | 경로 |
|------|------|
| 자동 제출 작업 | `tmk-batch/src/main/java/.../job/ExamAutoSubmitJob.java` |
| 정리 작업 | `tmk-batch/src/main/java/.../job/ExpiredVerificationCleanJob.java` |
| 배치 스케줄러 | `tmk-batch/src/main/java/.../schedule/BatchScheduler.java` |

### 설정 파일

| 항목 | 경로 |
|------|------|
| 빌드 설정 | `build.gradle`, `settings.gradle` |
| 도메인 설정 | `tmk-core/build.gradle` |
| API 설정 | `tmk-api/src/main/resources/application*.yml` |
| 배치 설정 | `tmk-batch/src/main/resources/application.yml` |

### 문서

| 항목 | 경로 |
|------|------|
| 프로젝트 개요 | `README.md` |
| Claude Code 가이드 | `CLAUDE.md` |
| API 명세서 | `docs/API 명세서.md` |
| 도메인 모델 설계 | `docs/도메인 모델 설계.md` |
| ERD 설계 | `docs/ERD 설계.md` |
| DDL 스크립트 | `docs/ddl.sql` |

---

## 모듈 간 통신 흐름

### 요청 흐름 (REST API)

```
1. HTTP 요청 수신
   /api/v1/exams (POST)

2. AuthController.createExam()
   - @PostMapping 매핑
   - 요청 본문 파싱
   - 인증 확인 (@PreAuthorize)

3. CreateExamServiceImpl (tmk-api)
   - UseCase 구현체
   - 도메인 로직 조정

4. ExamCreationService (tmk-core)
   - Exam aggregate 생성
   - 비즈니스 규칙 검증
   - ExamQuestion 세트 생성

5. ExamRepository.save() (tmk-api)
   - Repository 어댑터
   - JPA 저장

6. ExamJpaRepository.save()
   - Spring Data JPA
   - PostgreSQL 삽입

7. HTTP 응답 반환
   { "errorCode": "SUCCESS", "data": { "examId": ... } }
```

### 배치 흐름 (자동 제출)

```
1. Spring Scheduler 트리거
   - @Scheduled(fixedDelay = 60000)
   - 매 1분마다 ExamAutoSubmitJob 실행

2. ExamAutoSubmitJob
   - Step 정의

3. ExamAutoSubmitStep
   - Reader: 만료된 시험 조회
   - Processor: 제출 처리
   - Writer: 채점 결과 저장

4. ExpiredExamReader
   - ExamRepository.findExpiredExams()
   - 현재 시간 > 시작 시간 + 30분인 시험 조회

5. ExamSubmitProcessor
   - ExamGradingService.grade()
   - 채점 로직 실행

6. ExamGradeWriter
   - Exam 상태 업데이트 (SUBMITTED → GRADED)
   - 채점 결과 저장
```

---

## 확장 가능성 및 향후 개선

### 향후 추가될 모듈

```
tmk-parent/
├── tmk-core/
├── tmk-api/
├── tmk-batch/
│
├── tmk-worker/           # (향후) 마이크로서비스: 비동기 작업
│   └── document-processor/ # PDF 파싱, 임베딩 생성
│
├── tmk-admin/            # (향후) 관리자 대시보드
│   └── REST API (Spring Boot)
│
├── tmk-mobile/           # (향후) 모바일 앱 백엔드
│   └── REST API (Spring Boot)
│
└── tmk-common/           # (향후) 공유 라이브러리
    └── 공통 유틸, 상수, 예외
```

### 확장성 설계 원칙

1. **UseCase 추가**: `tmk-core/port/in` 에 새 인터페이스 추가 → `tmk-api/adapter/in` 에 구현체 추가
2. **Domain Service 추가**: `tmk-core/domain` 에 새 Service 클래스 추가
3. **배치 작업 추가**: `tmk-batch/job` 에 새 Job 클래스 추가
4. **외부 통합**: 항상 `tmk-api/adapter` 를 통해 구현 (core 순수성 유지)

---

**마지막 수정**: 2026년 3월
**문서 버전**: 1.0.0
