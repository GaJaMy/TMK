# TMK 모듈 상세 구조

## tmk-core: 순수 도메인 모듈

### 개요
순수 비즈니스 로직을 담은 모듈로, **Spring, JPA, Redis 등 외부 프레임워크에 의존하지 않습니다.**
테스트 작성이 간편하고, 프레임워크 업그레이드 영향을 받지 않습니다.

### 패키지 구조

```
tmk-core/
├── src/main/java/com/tmk/core/
│   ├── domain/
│   │   ├── user/
│   │   │   ├── User.java                    # Aggregate Root
│   │   │   ├── UserProvider.java            # Enum (LOCAL, GOOGLE, NAVER, KAKAO)
│   │   │   └── UserStatus.java              # Enum (ACTIVE, INACTIVE)
│   │   │
│   │   ├── emailverification/
│   │   │   ├── EmailVerification.java       # Aggregate Root
│   │   │   ├── VerificationStatus.java      # Enum (PENDING, VERIFIED, EXPIRED)
│   │   │   └── VerificationCodeGenerator.java  # Value Object
│   │   │
│   │   ├── document/
│   │   │   ├── Document.java                # Aggregate Root
│   │   │   ├── DocumentStatus.java          # Enum (UPLOADED, PROCESSING, COMPLETED, FAILED)
│   │   │   ├── DocumentChunk.java           # Entity (Text + Vector)
│   │   │   ├── DocumentContent.java         # Value Object
│   │   │   └── ChunkingStrategy.java        # Strategy Pattern
│   │   │
│   │   ├── question/
│   │   │   ├── Question.java                # Aggregate Root
│   │   │   ├── QuestionType.java            # Enum (MULTIPLE_CHOICE, SHORT_ANSWER, ESSAY)
│   │   │   ├── QuestionOption.java          # Value Object
│   │   │   ├── QuestionDifficulty.java      # Enum (EASY, MEDIUM, HARD)
│   │   │   └── QuestionContent.java         # Value Object
│   │   │
│   │   ├── exam/
│   │   │   ├── Exam.java                    # Aggregate Root
│   │   │   ├── ExamStatus.java              # Enum (CREATED, IN_PROGRESS, COMPLETED, SUBMITTED)
│   │   │   ├── ExamAnswer.java              # Value Object
│   │   │   ├── ExamGrade.java               # Value Object (점수, 등급)
│   │   │   ├── ExamCreationService.java     # Domain Service
│   │   │   └── ExamGradingService.java      # Domain Service
│   │   │
│   │   └── shared/
│   │       ├── DomainEvent.java             # Base Class
│   │       └── AggregateRoot.java           # Base Class
│   │
│   └── port/
│       ├── in/
│       │   ├── auth/
│       │   │   ├── SendEmailVerificationUseCase.java
│       │   │   ├── VerifyEmailUseCase.java
│       │   │   ├── RegisterUseCase.java
│       │   │   ├── LoginUseCase.java
│       │   │   ├── LogoutUseCase.java
│       │   │   ├── ReissueTokenUseCase.java
│       │   │   ├── SocialLoginUseCase.java
│       │   │   └── dto/
│       │   │       ├── LoginCommand.java
│       │   │       ├── LoginResult.java
│       │   │       ├── ReissueResult.java
│       │   │       └── SocialLoginResult.java
│       │   │
│       │   ├── document/
│       │   │   ├── RegisterDocumentUseCase.java
│       │   │   ├── GetDocumentStatusUseCase.java
│       │   │   └── dto/
│       │   │       ├── RegisterDocumentCommand.java
│       │   │       ├── RegisterDocumentResult.java
│       │   │       └── DocumentStatusResult.java
│       │   │
│       │   ├── question/
│       │   │   ├── GetQuestionListUseCase.java
│       │   │   ├── GetQuestionDetailUseCase.java
│       │   │   └── dto/
│       │   │       ├── QuestionListResult.java
│       │   │       ├── QuestionDetailResult.java
│       │   │       ├── QuestionSummary.java
│       │   │       └── OptionResult.java
│       │   │
│       │   └── exam/
│       │       ├── CreateExamUseCase.java
│       │       ├── GetExamUseCase.java
│       │       ├── SaveAnswerUseCase.java
│       │       ├── SubmitExamUseCase.java
│       │       ├── GetExamResultUseCase.java
│       │       ├── GetExamHistoryUseCase.java
│       │       └── dto/
│       │           ├── CreateExamCommand.java
│       │           ├── AnswerCommand.java
│       │           ├── SubmitResult.java
│       │           ├── ExamResult.java
│       │           ├── ExamDetailResult.java
│       │           └── HistoryResult.java
│       │
│       └── out/
│           ├── UserRepository.java
│           ├── EmailVerificationRepository.java
│           ├── DocumentRepository.java
│           ├── DocumentChunkRepository.java
│           ├── QuestionRepository.java
│           └── ExamRepository.java
│
└── build.gradle  # 외부 의존성 최소화 (테스트만 포함)
```

### 주요 도메인 엔티티

#### User (사용자)
```java
// 핵심 규칙:
// - provider=LOCAL이면 password 필수
// - 소셜 로그인이면 providerId 필수
// - 중복 가입 방지
```

**속성**: id, email, password(선택), name, provider, providerId, status, createdAt, updatedAt

**메서드**:
- `verifyPassword(String password): boolean`
- `updateProfile(String name): void`
- `deactivate(): void`

#### EmailVerification (이메일 인증)
```java
// 핵심 규칙:
// - 코드 5분 유효
// - 인증 완료 후에만 회원가입 가능
// - 3회 실패 시 잠금
```

**속성**: id, email, code, status, attemptCount, expiresAt, verifiedAt

**메서드**:
- `verify(String code): boolean`
- `isExpired(): boolean`
- `isLocked(): boolean`

#### Document (문서)
```java
// 핵심 규칙:
// - 등록 즉시 처리 파이프라인 시작
// - 하나의 문서에서 최소 2개 문제 생성
// - 청킹 전략: 512개 토큰 크기, 128개 오버랩
```

**속성**: id, userId, fileName, status, originalFileName, uploadedAt, processingStartedAt, completedAt

**메서드**:
- `startProcessing(): void`
- `completeProcessing(int questionCount): void`
- `failProcessing(String errorMessage): void`

#### DocumentChunk (문서 청크)
```java
// 청킹된 텍스트 + 임베딩 벡터
// pgvector HNSW 인덱스로 빠른 유사도 검색
```

**속성**: id, documentId, text, embedding(vector 1536), chunkIndex, createdAt

#### Question (질문)
```java
// 핵심 규칙:
// - MULTIPLE_CHOICE: 5개 선택지 필수
// - SHORT_ANSWER: 선택지 없음
// - ESSAY: 선택지 없음
```

**속성**: id, documentId, type, difficulty, content, options[], correctAnswer, explanation

**메서드**:
- `getOptions(): List<QuestionOption>`
- `getCorrectAnswer(): String`
- `isValid(): boolean`

#### Exam (시험)
```java
// 핵심 규칙:
// - 최소 10문제
// - 난이도별 최소 1문제 (EASY, MEDIUM, HARD)
// - 정답률 50% 이상 합격
// - 제한 시간 초과 자동 제출
```

**속성**: id, userId, questionIds[], answers{}, status, startedAt, submittedAt, score, grade

**메서드**:
- `addAnswer(int questionId, String answer): void`
- `isTimeExpired(): boolean`
- `calculateScore(): ExamGrade`
- `submit(): void`

### UseCase 인터페이스 규칙

#### 명명 규칙
- 동사 + 목적어 + "UseCase"
- 예: SendEmailVerificationUseCase, CreateExamUseCase

#### 구조
```java
public interface SendEmailVerificationUseCase {
    SendEmailVerificationResult execute(SendEmailVerificationCommand command);
}

public class SendEmailVerificationCommand {
    private final String email;
    // Constructor, getters
}

public class SendEmailVerificationResult {
    private final String message;
    // Constructor, getters
}
```

#### 책임
- 단일 비즈니스 기능만 담당
- 도메인 엔티티와 DTO 변환
- 비즈니스 규칙 검증
- 외부 포트 호출

### Repository 인터페이스

```java
public interface UserRepository {
    void save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(String email);
    void delete(UserId id);
}

public interface QuestionRepository {
    void save(Question question);
    List<Question> findByDocumentId(DocumentId documentId);
    List<Question> findByDifficultyAndDocumentId(Difficulty difficulty, DocumentId documentId);
}

public interface ExamRepository {
    void save(Exam exam);
    Optional<Exam> findById(ExamId id);
    List<Exam> findByUserId(UserId userId);
}
```

### Domain Service

도메인에서만 처리 가능한 복잡한 비즈니스 로직:

#### ExamCreationService
```java
public class ExamCreationService {

    // 시험 생성 규칙 검증
    public Exam createExam(CreateExamCommand command, QuestionRepository repo) {
        // 1. 질문 수 검증 (최소 10개)
        // 2. 난이도별 분포 확인 (EASY, MEDIUM, HARD 각 최소 1개)
        // 3. Exam 엔티티 생성
        // 4. 시작 시간 기록
        return new Exam(...);
    }
}
```

#### ExamGradingService
```java
public class ExamGradingService {

    // 시험 채점 규칙
    public ExamGrade grade(Exam exam, QuestionRepository repo) {
        // 1. 정답 비교
        // 2. 점수 계산
        // 3. 합격/불합격 판정 (50% 이상)
        // 4. 등급 부여
        return new ExamGrade(...);
    }
}
```

## tmk-api: REST API 및 어댑터 모듈

### 개요
Spring Boot 프레임워크를 활용한 REST API 계층과 데이터 접근 어댑터입니다.

### 패키지 구조

```
tmk-api/
├── src/main/java/com/tmk/api/
│   ├── TmkApiApplication.java               # 진입점
│   │
│   ├── controller/
│   │   ├── auth/
│   │   │   └── AuthController.java          # POST /api/v1/auth/**
│   │   ├── document/
│   │   │   └── DocumentController.java      # GET /api/v1/documents/**
│   │   ├── question/
│   │   │   └── QuestionController.java      # GET /api/v1/questions/**
│   │   ├── exam/
│   │   │   └── ExamController.java          # GET/POST /api/v1/exams/**
│   │   └── internal/
│   │       └── InternalDocumentController.java  # POST /internal/v1/documents/**
│   │
│   ├── config/
│   │   ├── SecurityConfig.java              # Spring Security 설정
│   │   ├── JpaConfig.java                   # JPA/Hibernate 설정
│   │   ├── RedisConfig.java                 # Redis 설정
│   │   └── OpenAiConfig.java                # OpenAI API 클라이언트
│   │
│   ├── security/
│   │   ├── jwt/
│   │   │   ├── JwtTokenProvider.java        # 토큰 생성/검증
│   │   │   ├── JwtAuthenticationFilter.java # 요청 필터
│   │   │   └── JwtProperties.java           # JWT 설정
│   │   ├── handler/
│   │   │   ├── JwtAuthenticationEntryPoint.java  # 미인증 처리
│   │   │   └── JwtAccessDeniedHandler.java      # 권한 없음 처리
│   │   ├── CustomUserDetails.java           # Spring Security User
│   │   └── UserDetailsServiceImpl.java       # 사용자 조회
│   │
│   ├── adapter/
│   │   ├── in/
│   │   │   ├── auth/
│   │   │   │   └── AuthUseCase.java         # UseCase 구현
│   │   │   ├── document/
│   │   │   │   └── DocumentUseCase.java
│   │   │   ├── question/
│   │   │   │   └── QuestionUseCase.java
│   │   │   └── exam/
│   │   │       └── ExamUseCase.java
│   │   │
│   │   └── out/
│   │       ├── UserRepositoryImpl.java       # JPA 구현
│   │       ├── DocumentRepositoryImpl.java
│   │       ├── QuestionRepositoryImpl.java
│   │       ├── ExamRepositoryImpl.java
│   │       └── persistence/
│   │           ├── UserJpaEntity.java       # JPA @Entity
│   │           ├── DocumentJpaEntity.java
│   │           ├── QuestionJpaEntity.java
│   │           ├── ExamJpaEntity.java
│   │           └── jpa/
│   │               ├── UserJpaRepository.java     # Spring Data JPA
│   │               ├── DocumentJpaRepository.java
│   │               ├── QuestionJpaRepository.java
│   │               └── ExamJpaRepository.java
│   │
│   ├── common/
│   │   ├── ApiResponse.java                 # 표준 응답
│   │   ├── ErrorCode.java                   # 에러 코드 정의
│   │   └── GlobalExceptionHandler.java      # @ControllerAdvice
│   │
│   ├── service/
│   │   ├── EmailService.java                # 이메일 발송
│   │   ├── OpenAiService.java               # OpenAI 호출
│   │   ├── DocumentProcessingService.java   # PDF 처리
│   │   └── QuestionGenerationService.java   # 질문 생성
│   │
│   └── cache/
│       ├── EmailVerificationCache.java      # Redis 캐시
│       ├── JwtTokenCache.java
│       └── ExamAnswerCache.java
│
└── build.gradle
```

### Controller 구조

#### AuthController
```
POST   /api/v1/auth/send-verification    # 인증코드 발송
POST   /api/v1/auth/verify-email         # 이메일 인증
POST   /api/v1/auth/register             # 회원가입
POST   /api/v1/auth/login                # 로그인
POST   /api/v1/auth/logout               # 로그아웃
POST   /api/v1/auth/reissue              # 토큰 재발급
POST   /api/v1/auth/social-login/{provider}  # 소셜 로그인
```

#### DocumentController
```
GET    /api/v1/documents                 # 문서 목록
GET    /api/v1/documents/{id}            # 문서 상세
GET    /api/v1/documents/{id}/status     # 처리 상태 조회
```

#### QuestionController
```
GET    /api/v1/questions                 # 질문 목록 (페이지네이션)
GET    /api/v1/questions/{id}            # 질문 상세 (선택지 포함)
GET    /api/v1/documents/{id}/questions  # 특정 문서의 질문
```

#### ExamController
```
POST   /api/v1/exams                     # 시험 생성
GET    /api/v1/exams/{id}                # 시험 조회 (진행 중)
POST   /api/v1/exams/{id}/answers        # 답안 저장 (Redis)
POST   /api/v1/exams/{id}/submit         # 시험 제출 (채점)
GET    /api/v1/exams/{id}/result         # 결과 조회
GET    /api/v1/users/exams               # 시험 목록 (히스토리)
GET    /api/v1/users/exams/{id}          # 시험 상세 결과
```

#### InternalDocumentController
```
POST   /internal/v1/documents            # 문서 등록 (내부용)
```

### Adapter 구조

#### Incoming Adapter Pattern
```
HTTP Request
    ↓
[AuthController.login()]
    ↓
[DTO 검증 및 변환]
    ↓
[LoginUseCase.execute(command)]  ← Domain
    ↓
[HTTP Response]
```

#### Outgoing Adapter Pattern
```
[Domain UseCase]
    ↓
[UserRepository.findByEmail(email)]  ← Port Interface
    ↓
[UserRepositoryImpl.findByEmail(email)]  ← Adapter
    ↓
[UserJpaRepository.findByEmail(email)]  ← Spring Data JPA
    ↓
PostgreSQL
```

### 인증 흐름

```
1. 클라이언트 로그인 요청
   POST /api/v1/auth/login
   { "email": "user@example.com", "password": "..." }

2. AuthController → LoginUseCase
   - 이메일/비밀번호 검증
   - User 엔티티 조회

3. JwtTokenProvider
   - Access Token 생성 (1시간)
   - Refresh Token 생성 (Redis 저장, 7일)

4. 응답
   {
     "accessToken": "...",
     "refreshToken": "...",
     "expiresIn": 3600
   }

5. 후속 요청
   GET /api/v1/exams
   Authorization: Bearer {accessToken}

6. JwtAuthenticationFilter
   - 헤더에서 토큰 추출
   - JwtTokenProvider로 검증
   - SecurityContext에 사용자 정보 설정
```

## tmk-batch: 배치 처리 모듈

### 개요
Spring Batch를 활용한 스케줄된 작업 처리:

### 패키지 구조

```
tmk-batch/
├── src/main/java/com/tmk/batch/
│   ├── TmkBatchApplication.java         # 진입점
│   │
│   ├── job/
│   │   ├── exam/
│   │   │   ├── ExamAutoSubmitJob.java   # 1분마다 실행
│   │   │   ├── ExamAutoSubmitStep.java
│   │   │   ├── ExamAutoSubmitReader.java    # ItemReader
│   │   │   ├── ExamAutoSubmitProcessor.java # ItemProcessor
│   │   │   └── ExamAutoSubmitWriter.java    # ItemWriter
│   │   │
│   │   └── verification/
│   │       ├── ExpiredVerificationCleanJob.java   # 매일 새벽
│   │       ├── ExpiredVerificationCleanStep.java
│   │       ├── VerificationCleanReader.java
│   │       ├── VerificationCleanProcessor.java
│   │       └── VerificationCleanWriter.java
│   │
│   ├── config/
│   │   ├── BatchConfig.java             # Batch 설정
│   │   ├── JobSchedulingConfig.java     # 스케줄 설정
│   │   └── DatabaseConfig.java          # JobRepository 설정
│   │
│   ├── listener/
│   │   ├── JobExecutionListener.java    # Job 로그
│   │   ├── StepExecutionListener.java   # Step 로그
│   │   └── ExceptionSkipListener.java   # 에러 처리
│   │
│   └── service/
│       ├── ExamGradingBatchService.java # 채점 로직
│       └── VerificationCleanupService.java
│
└── build.gradle
```

### Job 설정

#### ExamAutoSubmitJob (1분 주기)
```java
// 1. 만료된 시험 조회
//    SELECT * FROM EXAMS
//    WHERE status = 'IN_PROGRESS'
//    AND started_at + '1 hour' < NOW()
//
// 2. 시험별로 처리
//    - 답안 조회 (Redis에서)
//    - 채점 실행 (ExamGradingService)
//    - 결과 저장 (DB)
//    - 상태 변경 (COMPLETED → SUBMITTED)
//
// 3. 로그 기록
```

**구성**:
- **Reader**: ExamAutoSubmitReader
  - 만료된 시험 (IN_PROGRESS 상태 + 1시간 초과) 조회
  - Chunk 단위로 읽음 (기본 100개)

- **Processor**: ExamAutoSubmitProcessor
  - 답안 Redis에서 조회
  - ExamGradingService로 채점
  - 결과 DTO 생성

- **Writer**: ExamAutoSubmitWriter
  - 시험 상태 업데이트 (SUBMITTED)
  - 결과 저장
  - Redis 캐시 정리

#### ExpiredVerificationCleanJob (매일 새벽 2시)
```java
// 1. 만료된 인증코드 조회
//    SELECT * FROM EMAIL_VERIFICATIONS
//    WHERE status = 'EXPIRED'
//    AND created_at < NOW() - '1 day'
//
// 2. 배치 삭제
//
// 3. 성공 로그
```

**구성**:
- **Reader**: VerificationCleanReader
  - 1일 이상 만료된 코드 조회

- **Processor**: VerificationCleanProcessor
  - 필터링 (삭제 대상 검증)

- **Writer**: VerificationCleanWriter
  - DB에서 삭제
  - Redis 캐시도 정리

### 스케줄 설정

```java
@Configuration
@EnableScheduling
public class JobSchedulingConfig {

    // ExamAutoSubmitJob: 매 1분마다
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void scheduleExamAutoSubmitJob() { ... }

    // ExpiredVerificationCleanJob: 매일 02:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleVerificationCleanJob() { ... }
}
```

## 모듈 간 통신 흐름

### 요청 처리 흐름

```
REST 요청
  ↓
[AuthController]           (tmk-api Incoming Adapter)
  ↓DTO 변환
[LoginUseCase]             (tmk-core Port/In)
  ↓ 비즈니스 로직
[User 도메인 엔티티]        (tmk-core Domain)
  ↓
[UserRepository]           (tmk-core Port/Out)
  ↓
[UserRepositoryImpl]        (tmk-api Outgoing Adapter)
  ↓
[Spring Data JPA]          (tmk-api Persistence)
  ↓
PostgreSQL
```

### 배치 실행 흐름

```
Spring Batch Scheduler
  ↓ (1분마다)
[ExamAutoSubmitJob]
  ↓
[ExamAutoSubmitReader]
  ↓ ExamRepository 호출
[만료된 시험 목록]
  ↓
[ExamAutoSubmitProcessor]
  ↓ 답안 조회, 채점 실행
[ExamGradingService]
  ↓
[ExamAutoSubmitWriter]
  ↓ 결과 저장
PostgreSQL + Redis
```

## 의존성 규칙

```
tmk-api
  ↓ 의존
tmk-core (외부 프레임워크 없음)

tmk-batch
  ↓ 의존
tmk-core (외부 프레임워크 없음)
```

**핵심**: tmk-core는 어떤 모듈도 의존하지 않습니다. 이를 통해 비즈니스 로직이 기술 결정으로부터 보호됩니다.
