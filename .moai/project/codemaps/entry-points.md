# TMK 애플리케이션 진입점 및 엔드포인트

## 애플리케이션 진입점

### TmkApiApplication (REST API 서버)

```java
@SpringBootApplication
public class TmkApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmkApiApplication.class, args);
    }
}
```

**기능**:
- Spring Boot 웹 서버 시작 (기본 포트: 8080)
- Spring Security 설정 활성화
- JPA 엔티티 매핑
- Redis 캐시 구성
- OpenAI API 클라이언트 초기화

**시작 시 실행되는 작업**:
1. 데이터베이스 연결 (PostgreSQL)
2. Redis 연결
3. JPA 메타데이터 생성
4. Spring Security Filter Chain 설정
5. JWT TokenProvider 초기화

### TmkBatchApplication (배치 처리 서버)

```java
@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class TmkBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmkBatchApplication.class, args);
    }
}
```

**기능**:
- Spring Batch 작업 스케줄러 시작
- JobRepository 설정 (Meta DB)
- Step/Job 리스너 등록
- 정기 작업 실행

**스케줄된 작업**:
1. ExamAutoSubmitJob (매 1분)
2. ExpiredVerificationCleanJob (매일 02:00 AM)

## REST API 엔드포인트 맵

### 인증 API (AuthController)

#### 1. 이메일 인증코드 발송

```
POST /api/v1/auth/send-verification
Content-Type: application/json

Request Body:
{
  "email": "user@example.com"
}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "인증코드가 이메일로 발송되었습니다.",
  "data": {
    "message": "이메일 확인 후 5분 내에 인증해주세요"
  }
}

Error (400 Bad Request):
{
  "errorCode": "INVALID_EMAIL",
  "msg": "유효하지 않은 이메일 형식입니다.",
  "data": null
}
```

**비즈니스 로직**:
1. 이메일 형식 검증
2. SendEmailVerificationUseCase 호출
3. 인증코드 생성 (6자리 숫자)
4. Redis에 저장 (TTL: 5분)
5. 이메일 발송

#### 2. 이메일 인증

```
POST /api/v1/auth/verify-email
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "code": "123456"
}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "이메일이 인증되었습니다.",
  "data": {
    "verified": true
  }
}

Error (400):
{
  "errorCode": "INVALID_CODE",
  "msg": "인증코드가 올바르지 않습니다.",
  "data": null
}
```

**비즈니스 로직**:
1. 코드 유효성 확인 (Redis)
2. 시도 횟수 확인 (3회 이상 실패 시 잠금)
3. 코드 일치 여부 확인
4. EmailVerification 상태 변경 (VERIFIED)
5. 회원가입 가능 상태로 전환

#### 3. 회원가입

```
POST /api/v1/auth/register
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "name": "홍길동"
}

Response (201 Created):
{
  "errorCode": "SUCCESS",
  "msg": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "name": "홍길동"
  }
}

Error (400):
{
  "errorCode": "EMAIL_ALREADY_EXISTS",
  "msg": "이미 가입된 이메일입니다.",
  "data": null
}
```

**비즈니스 로직**:
1. 이메일 인증 확인 (VERIFIED 상태)
2. 이메일 중복 확인
3. 비밀번호 강도 검증 (최소 8자, 대문자, 소문자, 숫자, 특수문자)
4. User 엔티티 생성 (provider=LOCAL)
5. 데이터베이스에 저장

#### 4. 로그인

```
POST /api/v1/auth/login
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_value...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}

Error (401):
{
  "errorCode": "INVALID_CREDENTIALS",
  "msg": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

**비즈니스 로직**:
1. 이메일로 User 조회
2. 비밀번호 검증
3. Access Token 생성 (유효기간: 1시간)
4. Refresh Token 생성 및 Redis 저장 (유효기간: 7일)
5. 토큰 반환

#### 5. 토큰 재발급

```
POST /api/v1/auth/reissue
Content-Type: application/json

Request Header:
Authorization: Bearer {refreshToken}

Request Body:
{
  "refreshToken": "refresh_token_value..."
}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "토큰 재발급 완료",
  "data": {
    "accessToken": "new_access_token...",
    "refreshToken": "new_refresh_token...",
    "expiresIn": 3600
  }
}

Error (401):
{
  "errorCode": "INVALID_REFRESH_TOKEN",
  "msg": "리프레시 토큰이 유효하지 않습니다.",
  "data": null
}
```

**비즈니스 로직**:
1. Refresh Token 검증 (Redis)
2. 사용자 정보 조회
3. 새로운 Access Token 생성
4. 새로운 Refresh Token 생성 (선택적 로테이션)
5. 기존 Refresh Token 삭제

#### 6. 소셜 로그인

```
POST /api/v1/auth/social-login/google
Content-Type: application/json

Request Body:
{
  "idToken": "google_oauth_id_token...",
  "provider": "GOOGLE"
}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "소셜 로그인 성공",
  "data": {
    "userId": 5,
    "accessToken": "...",
    "refreshToken": "...",
    "isNewUser": false
  }
}
```

**지원 제공자**: GOOGLE, NAVER, KAKAO

**비즈니스 로직**:
1. 제공자별 ID 토큰 검증
2. 사용자 정보 추출 (email, name)
3. 기존 사용자 확인
4. 신규 사용자면 자동 가입 (provider=GOOGLE/NAVER/KAKAO)
5. 토큰 발급

#### 7. 로그아웃

```
POST /api/v1/auth/logout
Content-Type: application/json

Request Header:
Authorization: Bearer {accessToken}

Request Body:
{
  "refreshToken": "refresh_token_value..."
}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "로그아웃 되었습니다.",
  "data": null
}
```

**비즈니스 로직**:
1. Refresh Token을 Redis 블랙리스트에 등록
2. Access Token도 블랙리스트에 등록 (선택적)

### 문서 API (DocumentController)

#### 1. 문서 목록 조회

```
GET /api/v1/documents?page=0&size=20
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "content": [
      {
        "id": 1,
        "fileName": "java-basics.pdf",
        "status": "COMPLETED",
        "uploadedAt": "2025-03-12T10:30:00Z",
        "questionCount": 42
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "currentPage": 0
  }
}
```

#### 2. 문서 상세 조회

```
GET /api/v1/documents/{documentId}
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "id": 1,
    "fileName": "java-basics.pdf",
    "status": "COMPLETED",
    "uploadedAt": "2025-03-12T10:30:00Z",
    "processingStartedAt": "2025-03-12T10:31:00Z",
    "completedAt": "2025-03-12T10:45:00Z",
    "questionCount": 42,
    "estimatedReadTime": 15
  }
}
```

#### 3. 문서 처리 상태 조회

```
GET /api/v1/documents/{documentId}/status
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "status": "PROCESSING",
    "progress": 65,
    "processedChunks": 65,
    "totalChunks": 100,
    "estimatedTimeRemaining": 120,
    "currentStep": "문제 생성 중..."
  }
}
```

#### 내부 API: 문서 등록

```
POST /internal/v1/documents
Content-Type: multipart/form-data

Request:
file: [PDF 파일]

Response (201 Created):
{
  "errorCode": "SUCCESS",
  "msg": "문서가 등록되었습니다.",
  "data": {
    "documentId": 1,
    "fileName": "java-basics.pdf",
    "status": "UPLOADED",
    "processingStartedAt": "2025-03-12T10:31:00Z"
  }
}
```

**처리 파이프라인**:
1. PDF 업로드 및 검증
2. 텍스트 추출 (Apache PDFBox)
3. 청킹 (512 토큰, 128 오버랩)
4. 임베딩 생성 (OpenAI text-embedding-3-small)
5. pgvector에 저장
6. 질문 생성 (OpenAI GPT)
7. QUESTION 테이블에 저장

### 질문 API (QuestionController)

#### 1. 질문 목록 조회

```
GET /api/v1/questions?documentId=1&page=0&size=20
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "MULTIPLE_CHOICE",
        "difficulty": "EASY",
        "content": "Java의 장점은?",
        "preview": "Java의 장점은?",
        "documentTitle": "java-basics.pdf"
      }
    ],
    "totalElements": 42,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

#### 2. 질문 상세 조회 (선택지 포함)

```
GET /api/v1/questions/{questionId}
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "id": 1,
    "type": "MULTIPLE_CHOICE",
    "difficulty": "EASY",
    "content": "Java의 장점은?",
    "options": [
      {
        "optionId": 1,
        "text": "플랫폼 독립적이다",
        "isCorrect": true
      },
      {
        "optionId": 2,
        "text": "빠른 실행 속도",
        "isCorrect": false
      }
    ],
    "explanation": "Java는 JVM 위에서 실행되므로 플랫폼 독립적입니다."
  }
}
```

### 시험 API (ExamController)

#### 1. 시험 생성

```
POST /api/v1/exams
Content-Type: application/json
Authorization: Bearer {accessToken}

Request Body:
{
  "documentIds": [1, 2, 3],
  "questionCount": 10,
  "timeLimit": 3600
}

Response (201 Created):
{
  "errorCode": "SUCCESS",
  "msg": "시험이 생성되었습니다.",
  "data": {
    "examId": 5,
    "questionCount": 10,
    "timeLimit": 3600,
    "startedAt": "2025-03-12T14:00:00Z",
    "expiresAt": "2025-03-12T15:00:00Z",
    "questions": [
      {
        "questionId": 1,
        "type": "MULTIPLE_CHOICE",
        "difficulty": "EASY",
        "content": "질문 내용"
      }
    ]
  }
}

Error (400):
{
  "errorCode": "INVALID_EXAM_CONFIGURATION",
  "msg": "시험 구성이 올바르지 않습니다. 최소 10개 질문 필요",
  "data": null
}
```

**검증 규칙**:
- 최소 10문제
- 난이도별 최소 1문제 (EASY, MEDIUM, HARD)
- 제한 시간: 기본 1시간

#### 2. 진행 중인 시험 조회

```
GET /api/v1/exams/{examId}
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 5,
    "status": "IN_PROGRESS",
    "currentQuestionIndex": 3,
    "totalQuestions": 10,
    "timeRemaining": 2400,
    "currentQuestion": {
      "questionId": 4,
      "type": "MULTIPLE_CHOICE",
      "content": "다음 중 맞는 것은?",
      "options": [...]
    },
    "answeredCount": 3,
    "unansweredCount": 7
  }
}
```

#### 3. 답안 저장 (임시)

```
POST /api/v1/exams/{examId}/answers
Content-Type: application/json
Authorization: Bearer {accessToken}

Request Body:
{
  "questionId": 4,
  "answer": "1"  # 선택지 ID 또는 텍스트
}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "답안이 저장되었습니다.",
  "data": {
    "questionId": 4,
    "answer": "1",
    "savedAt": "2025-03-12T14:05:00Z"
  }
}
```

**저장 방식**:
- 임시 저장: Redis (TTL: 시험 종료까지)
- 시험 제출 시: PostgreSQL로 이동

#### 4. 시험 제출 및 채점

```
POST /api/v1/exams/{examId}/submit
Content-Type: application/json
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "시험이 채점되었습니다.",
  "data": {
    "examId": 5,
    "status": "SUBMITTED",
    "totalQuestions": 10,
    "correctAnswers": 7,
    "score": 70,
    "grade": "C+",
    "passed": true,
    "submittedAt": "2025-03-12T14:50:00Z"
  }
}
```

**채점 로직**:
1. 각 질문별 정답 비교
2. 점수 계산: (정답 수 / 총 문제 수) × 100
3. 합격/불합격 판정: 50점 이상 합격
4. 등급 부여: 90-100 A, 80-89 B, 70-79 C, 60-69 D, 0-59 F

#### 5. 시험 결과 조회

```
GET /api/v1/exams/{examId}/result
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 5,
    "status": "COMPLETED",
    "totalQuestions": 10,
    "correctAnswers": 7,
    "wrongAnswers": 3,
    "score": 70,
    "grade": "C+",
    "passed": true,
    "timeSpent": 1800,
    "submittedAt": "2025-03-12T14:50:00Z",
    "detailedResults": [
      {
        "questionId": 1,
        "userAnswer": "1",
        "correctAnswer": "1",
        "correct": true,
        "explanation": "..."
      }
    ]
  }
}
```

#### 6. 시험 히스토리 (목록)

```
GET /api/v1/users/exams?page=0&size=10
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "content": [
      {
        "examId": 5,
        "documentTitles": ["java-basics.pdf", "spring-framework.pdf"],
        "score": 70,
        "grade": "C+",
        "passed": true,
        "submittedAt": "2025-03-12T14:50:00Z"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

#### 7. 시험 상세 히스토리

```
GET /api/v1/users/exams/{examId}/history
Authorization: Bearer {accessToken}

Response (200 OK):
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 5,
    "documentTitles": ["java-basics.pdf"],
    "totalQuestions": 10,
    "correctAnswers": 7,
    "score": 70,
    "grade": "C+",
    "passed": true,
    "timeSpent": 1800,
    "submittedAt": "2025-03-12T14:50:00Z",
    "questionResults": [...]
  }
}
```

## Spring Batch 작업

### ExamAutoSubmitJob (매 1분 실행)

```java
@Configuration
@EnableBatchProcessing
public class ExamAutoSubmitJobConfig {

    @Bean
    public Job examAutoSubmitJob(JobBuilderFactory jobBuilder,
                                  Step examAutoSubmitStep) {
        return jobBuilder.get("examAutoSubmitJob")
            .start(examAutoSubmitStep)
            .build();
    }

    @Bean
    public Step examAutoSubmitStep(StepBuilderFactory stepBuilder,
                                    ItemReader<Exam> reader,
                                    ItemProcessor<Exam, ExamSubmitResult> processor,
                                    ItemWriter<ExamSubmitResult> writer) {
        return stepBuilder.get("examAutoSubmitStep")
            .<Exam, ExamSubmitResult>chunk(100)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
}
```

**실행 흐름**:
```
1분마다
  ↓
[Reader] 만료된 시험 조회
  - status = 'IN_PROGRESS'
  - started_at + 1시간 < NOW()
  ↓
[Processor] 각 시험 처리
  - Redis에서 답안 조회
  - ExamGradingService로 채점
  - ExamSubmitResult 생성
  ↓
[Writer] 결과 저장
  - Exam.status = 'SUBMITTED'
  - ExamResult 저장
  - Redis 캐시 정리
  ↓
로그 기록
```

### ExpiredVerificationCleanJob (매일 02:00 AM)

```
1. 1일 이상 만료된 이메일 인증 조회
   SELECT * FROM EMAIL_VERIFICATIONS
   WHERE status IN ('EXPIRED', 'PENDING')
   AND created_at < NOW() - INTERVAL 1 day

2. 배치 삭제
   - 100개씩 청크로 처리
   - 트랜잭션 단위로 커밋

3. Redis 캐시 정리
   - 만료된 코드 삭제

4. 로그 기록
   - 삭제된 행 수
   - 실행 시간
```

## 권한 검증 (Spring Security)

### 보호된 엔드포인트

```
모든 /api/v1/** 엔드포인트는 JWT 인증 필수

[HTTP 요청]
  ↓ 헤더에서 Authorization 추출
[JwtAuthenticationFilter]
  ↓ Bearer 토큰 파싱
[JwtTokenProvider.validateToken()]
  ↓ 서명 검증
[SecurityContext.setAuthentication()]
  ↓ 인증 정보 설정
[Controller] 요청 처리
```

### 공개 엔드포인트

```
POST /api/v1/auth/send-verification     (인증 필요 X)
POST /api/v1/auth/verify-email          (인증 필요 X)
POST /api/v1/auth/register              (인증 필요 X)
POST /api/v1/auth/login                 (인증 필요 X)
POST /api/v1/auth/reissue               (인증 필요 X)
POST /api/v1/auth/social-login/**       (인증 필요 X)
```

## 에러 처리 & 응답 코드

### HTTP 상태 코드

| 상태 코드 | 의미 | 예시 |
|---------|------|------|
| 200 | OK | 성공한 GET/PUT 요청 |
| 201 | Created | 성공한 POST 요청 |
| 400 | Bad Request | 입력 검증 실패 |
| 401 | Unauthorized | 토큰 없음/만료 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 500 | Internal Server Error | 서버 오류 |

### API 에러 코드

| 에러 코드 | HTTP 상태 | 설명 |
|----------|---------|------|
| AUTH_001 | 400 | 잘못된 이메일 형식 |
| AUTH_002 | 409 | 이메일 중복 |
| AUTH_003 | 401 | 비밀번호 불일치 |
| AUTH_004 | 401 | 토큰 만료 |
| EXAM_001 | 400 | 무효한 시험 구성 |
| EXAM_002 | 404 | 시험 없음 |
| QUESTION_001 | 400 | 무효한 질문 |
| COMMON_001 | 500 | 서버 오류 |

## 성능 고려사항

### 페이지네이션
```
GET /api/v1/questions?page=0&size=20

기본: page=0, size=20
최대: size=100
```

### 캐싱 전략
```
- 질문 목록: 1시간 (Redis)
- 사용자 정보: 30분 (Redis)
- 시험 진행 중 답안: 영구 (시험 제출 시 DB로 이동)
```

### 연결 풀
```
- 데이터베이스: HikariCP (poolSize=10)
- Redis: Lettuce (연결 풀 활성화)
```
