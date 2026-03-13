# TMK 아키텍처 개요

## 프로젝트 소개

**TMK (Test My Knowledge)** 는 AI 기반의 문제은행 플랫폼입니다. 사용자가 PDF 문서를 등록하면 OpenAI를 통해 자동으로 문제를 생성하고, 학습자가 시험을 응시하여 학습 이해도를 확인할 수 있습니다.

### 핵심 기술 스택

- **언어 및 프레임워크**: Java 21, Spring Boot 3.5.11
- **빌드 도구**: Gradle (멀티 모듈)
- **데이터베이스**: PostgreSQL + pgvector 확장
- **캐시**: Redis (이메일 인증코드, JWT refresh token, 시험 답안)
- **AI 통합**: OpenAI API (임베딩, 문제 생성)

## 아키텍처 개요

### 계층별 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                     REST API Layer (tmk-api)                    │
│  - Spring Boot Application                                       │
│  - REST Controllers (인증, 문서, 질문, 시험)                      │
│  - Spring Security + JWT 인증                                    │
├─────────────────────────────────────────────────────────────────┤
│                   Adapter Layer (tmk-api)                        │
│  - Incoming Adapters: Controller → UseCase 변환                 │
│  - Outgoing Adapters: Repository 구현체 (JPA)                   │
├─────────────────────────────────────────────────────────────────┤
│              Application / Use Case Layer (tmk-core)             │
│  - UseCase 인터페이스 (port/in/)                                 │
│  - 비즈니스 로직 (도메인 서비스)                                  │
│  - DTO 변환                                                      │
├─────────────────────────────────────────────────────────────────┤
│           Domain Layer (tmk-core - Framework 독립적)             │
│  - Aggregate Root (User, Document, Question, Exam)              │
│  - Entity, Value Object, Domain Event                           │
│  - Domain Service (ExamCreationService, ExamGradingService)    │
├─────────────────────────────────────────────────────────────────┤
│                    Port Layer (tmk-core)                         │
│  - Outgoing Ports: Repository 인터페이스                         │
│  - 외부 시스템과의 계약 정의                                      │
├─────────────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                            │
│  - PostgreSQL (JPA 매핑)                                         │
│  - Redis (캐시, 세션)                                            │
│  - OpenAI API (임베딩, LLM)                                       │
├─────────────────────────────────────────────────────────────────┤
│              Batch Processing Layer (tmk-batch)                  │
│  - Spring Batch Job (ExamAutoSubmitJob, ExpiredVerificationCleanJob) │
│  - 스케줄된 작업 실행                                             │
└─────────────────────────────────────────────────────────────────┘
```

## 모듈 책임

### tmk-core 모듈
**순수 도메인 로직 - Spring/JPA 프레임워크 의존성 없음**

- **domain/**: 비즈니스 규칙 집중화
  - User: 사용자 엔티티, 로그인 제공자(LOCAL, GOOGLE, NAVER, KAKAO)
  - EmailVerification: 이메일 인증 규칙 (5분 유효기간)
  - Document: 문서 등록, 청킹, 임베딩 처리 상태
  - Question: 질문 유형 (MULTIPLE_CHOICE 5개 선택지 필수)
  - Exam: 시험 생성, 채점 규칙 (최소 10문제, 50% 정답률 합격)

- **port/in/**: UseCase 인터페이스 (입력 포트)
  - AuthUseCase: 회원가입, 로그인, JWT 재발급, 소셜 로그인
  - DocumentUseCase: 문서 등록, 처리 상태 조회
  - QuestionUseCase: 질문 목록, 상세 조회
  - ExamUseCase: 시험 생성, 조회, 답안 저장, 제출, 결과 조회

- **port/out/**: Repository 인터페이스 (출력 포트)
  - UserRepository, EmailVerificationRepository
  - DocumentRepository, DocumentChunkRepository
  - QuestionRepository, ExamRepository

### tmk-api 모듈
**REST API 구현 - Spring Security, Spring Data JPA**

- **controller/**: 공개 REST 엔드포인트
  - AuthController: `/api/v1/auth/**` (회원가입, 로그인, 재발급)
  - DocumentController: `/api/v1/documents/**` (문서 관리)
  - QuestionController: `/api/v1/questions/**` (질문 조회)
  - ExamController: `/api/v1/exams/**` (시험 응시)
  - InternalDocumentController: `/internal/v1/documents/**` (내부용 문서 등록)

- **security/**: JWT 인증
  - JwtAuthenticationFilter: 요청 헤더에서 토큰 추출, 검증
  - JwtAuthenticationEntryPoint: 미인증 요청 처리
  - JwtAccessDeniedHandler: 권한 없는 요청 처리
  - UserDetailsServiceImpl: Spring Security 사용자 로딩

- **adapter/**: 포트-어댑터 변환
  - in/: Controller → UseCase 호출
  - out/: Repository 구현체 (JPA)

- **common/**: 공통 응답 형식
  - ApiResponse: `{ "errorCode", "msg", "data" }` 표준화

### tmk-batch 모듈
**Spring Batch - 스케줄된 작업**

- **ExamAutoSubmitJob**: 매 1분마다 실행
  - 만료된 시험 자동 제출
  - 채점 실행
  - 결과 저장

- **ExpiredVerificationCleanJob**: 매일 새벽 실행
  - 만료된 이메일 인증코드 삭제
  - Redis, DB 정리

## 클린 아키텍처 원칙

### 의존성 방향
```
tmk-api (REST, 프레임워크) → tmk-core (순수 도메인) ← tmk-batch (배치)
↓                          ↑
PostgreSQL, Redis, OpenAI API
```

**핵심 규칙**: tmk-core는 외부 프레임워크(Spring, JPA, Redis 등)에 의존하지 않습니다.
- 비즈니스 로직이 프레임워크 선택으로부터 독립적
- 단위 테스트 용이 (외부 의존성 모킹 불필요)
- 다른 프레임워크로 마이그레이션 가능

### 포트 & 어댑터 패턴

**Incoming Adapter** (Primary Adapter):
- REST 요청 → Controller → UseCase 호출
- 컨트롤러는 DTO 변환, 검증만 담당

**Outgoing Adapter** (Secondary Adapter):
- Repository 인터페이스 → JPA 구현체
- 데이터 액세스 로직 분리

```
HTTP Request
    ↓
[AuthController]  (Incoming Adapter)
    ↓
[SendEmailVerificationUseCase]  (Domain UseCase)
    ↓
[EmailVerificationRepository]  (Outgoing Port/Interface)
    ↓
[EmailVerificationRepositoryImpl]  (Outgoing Adapter)
    ↓
PostgreSQL
```

## 도메인 모델 규칙

| 도메인 | Aggregate Root | 주요 규칙 | 상태 관리 |
|--------|---------------|----------|----------|
| **User** | User | provider=LOCAL이면 password 필수 / 소셜이면 providerId 필수 | ACTIVE, INACTIVE |
| **EmailVerification** | EmailVerification | 코드 5분 유효 / 인증 완료 후만 회원가입 가능 | PENDING, VERIFIED, EXPIRED |
| **Document** | Document | 등록 즉시 처리 파이프라인 시작 / 최소 2개 문제 생성 | UPLOADED, PROCESSING, COMPLETED, FAILED |
| **Question** | Question | MULTIPLE_CHOICE는 5개 선택지 필수 / 나머지는 선택지 없음 | PUBLISHED, DRAFT |
| **Exam** | Exam | 최소 10문제 / 난이도별 최소 1문제 / 50% 이상 합격 | CREATED, IN_PROGRESS, COMPLETED, SUBMITTED |

## 시스템 컨텍스트 다이어그램

```
┌─────────────┐
│   사용자     │
│  (브라우저)  │
└──────┬──────┘
       │ HTTPS
       ▼
┌──────────────────────────────────┐
│      TMK REST API Server          │
│  (Spring Boot + Spring Security)  │
│  - Authentication Endpoints       │
│  - Document Management            │
│  - Question Access                │
│  - Exam Creation & Submission     │
└──┬────────────────────────────┬──┘
   │                            │
   │ JDBC                       │ Redis
   ▼                            ▼
┌──────────────────┐      ┌──────────────┐
│  PostgreSQL      │      │    Redis     │
│  - Users         │      │ - Auth Codes │
│  - Documents     │      │ - JWT Tokens │
│  - Chunks        │      │ - Exam Temp  │
│  - Questions     │      └──────────────┘
│  - Exams         │
│  - Answers       │
└──────────────────┘
        ▲
        │ pgvector 확장
        │ HNSW Index
        │ Cosine Similarity

┌──────────────────────────────┐
│   Batch Processing Server    │
│   (Spring Batch)             │
│ - ExamAutoSubmitJob (1분)   │
│ - ExpiredVerificationClean   │
│   Job (매일 새벽)             │
└──────────────────────────────┘

┌──────────────────┐
│   OpenAI API     │
│ - Embeddings     │
│ - Question Gen   │
└──────────────────┘
```

## 주요 설계 결정

### 1. 클린 아키텍처 도입
**이유**: 비즈니스 규칙이 기술 프레임워크로부터 독립되어야 함
- tmk-core는 순수 도메인 로직만 포함
- 프레임워크 업그레이드 시 도메인 코드 변경 불필요
- 단위 테스트 작성이 간단함

### 2. Hexagonal Architecture (포트 & 어댑터)
**이유**: 다양한 입출력 채널을 유연하게 지원
- REST API, 배치 작업, CLI 등 다양한 인터페이스 가능
- 외부 시스템(DB, 캐시, AI) 교체 용이
- 테스트용 Mock 어댑터 구현 간편

### 3. Use Case 기반 구조
**이유**: 비즈니스 요구사항과 코드 구조 일치
- SendEmailVerification, Login, CreateExam 등 각 UseCase는 하나의 기능
- 기능별 책임이 명확함
- 새로운 기능 추가 시 기존 코드 영향 최소화

### 4. PostgreSQL + pgvector
**이유**: 벡터 임베딩 기반 문서 검색
- 코사인 유사도를 통한 의미 기반 검색
- HNSW 인덱스로 대규모 데이터셋 성능 보장
- 기존 SQL 쿼리와 벡터 쿼리 통합 가능

### 5. Redis 캐시
**이유**: 높은 가용성과 빠른 응답 시간
- 이메일 인증코드 (5분 TTL)
- JWT refresh token 관리
- 시험 진행 중 임시 답안 저장
- 세션 데이터 캐싱

### 6. 배치 처리 (Spring Batch)
**이유**: 대량의 정기적 작업 자동화
- ExamAutoSubmitJob: 만료된 시험 자동 제출 (1분 주기)
- ExpiredVerificationCleanJob: 만료 데이터 정리 (일일 주기)

## API 규칙

### 표준 응답 형식
```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": { ... }
}
```

### 엔드포인트 분류
- **공개 API**: `/api/v1/**`
  - AuthController: 회원가입, 로그인, 토큰 재발급
  - QuestionController: 질문 조회
  - ExamController: 시험 응시

- **내부 API**: `/internal/v1/**`
  - DocumentController: 문서 등록 (관리자만)

### 인증 방식
- Bearer Token (JWT)
- 요청 헤더: `Authorization: Bearer {accessToken}`
- Access Token: 1시간 유효
- Refresh Token: Redis에 저장

### 에러 코드 패턴
- `AUTH_00x`: 인증 관련 에러
- `EXAM_00x`: 시험 관련 에러
- `QUESTION_00x`: 질문 관련 에러
- `COMMON_00x`: 공통 에러

## 데이터베이스 설계

### 특이사항
- `TIMESTAMPTZ`: JPA에서 `OffsetDateTime` 또는 `Instant` 매핑
- `BIGSERIAL`: `GenerationType.IDENTITY` 또는 `SEQUENCE` 사용
- Enum 컬럼: PostgreSQL 네이티브 ENUM 대신 `VARCHAR + CHECK` (JPA 호환성)
- `DOCUMENT_CHUNK.embedding`: `vector(1536)` 타입 (text-embedding-3-small)
- **HNSW 인덱스**: `m=16, ef_construction=64` (코사인 유사도)

### 주요 테이블
- **USERS**: 사용자 정보 (provider, providerId, password)
- **EMAIL_VERIFICATIONS**: 인증코드 (TTL 5분)
- **DOCUMENTS**: 등록된 문서 (상태: UPLOADED, PROCESSING, COMPLETED)
- **DOCUMENT_CHUNKS**: 청킹된 텍스트 + 벡터 임베딩
- **QUESTIONS**: 생성된 질문 (유형: MULTIPLE_CHOICE, SHORT_ANSWER 등)
- **QUESTION_OPTIONS**: 객관식 선택지
- **EXAMS**: 사용자 시험 (상태: CREATED, IN_PROGRESS, COMPLETED)
- **ANSWERS**: 시험 답안 (Redis 임시 저장 → DB 저장)

## 다음 단계

- [modules.md](./modules.md) - 각 모듈 상세 구조
- [dependencies.md](./dependencies.md) - 외부 의존성 관리
- [entry-points.md](./entry-points.md) - 애플리케이션 진입점
- [data-flow.md](./data-flow.md) - 데이터 처리 흐름
