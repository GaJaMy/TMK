# TMK 데이터 흐름 분석

## 1. 문서 처리 파이프라인

### 전체 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│                    문서 처리 파이프라인                           │
└─────────────────────────────────────────────────────────────────┘

사용자
  │
  ▼
[내부 API 호출]
POST /internal/v1/documents (PDF 파일)
  │
  ▼
[DocumentController]
  │ 파일 검증 (확장자, 크기)
  ▼
[DocumentUseCase]
  │ Document 엔티티 생성
  │ 상태: UPLOADED
  ▼
PostgreSQL에 저장
  │ (document row)
  ▼
[DocumentProcessingService]
  │ 비동기 처리 시작
  ▼
[1단계: PDF 텍스트 추출]
  │ Apache PDFBox 사용
  │ UTF-8 인코딩 처리
  ▼
[2단계: 청킹]
  │ 토큰 기반 청킹 (512 토큰)
  │ 오버랩: 128 토큰
  │ 문단 경계 존중
  ▼
[3단계: 임베딩 생성]
  │ OpenAI API 호출
  │ Model: text-embedding-3-small
  │ Dimension: 1536
  │ 배치 처리 (최대 100개/요청)
  ▼
[4단계: pgvector에 저장]
  │ DocumentChunk 엔티티
  │ embedding: vector(1536)
  │ HNSW 인덱스 자동 생성
  ▼
[5단계: 질문 생성]
  │ OpenAI GPT 호출
  │ 청크별로 질문 생성
  │ 검증 (중복 제거, 포맷)
  ▼
[6단계: 질문 저장]
  │ Question 엔티티
  │ QuestionOption 엔티티
  │ 최소 2개 이상 생성 필수
  ▼
[7단계: 완료]
  │ Document.status = COMPLETED
  │ 처리 시간 기록
  ▼
PostgeSQL 업데이트
  │
  ▼
사용자 알림 (선택)
```

### 각 단계 상세

#### 1단계: PDF 텍스트 추출

```java
// Apache PDFBox 사용
PDDocument document = PDDocument.load(pdfFile);
PDFTextStripper stripper = new PDFTextStripper();
String text = stripper.getText(document);

// 처리
// - 페이지 번호 제거
// - 특수 문자 정규화
// - 공백 정규화
// - 인코딩 처리 (한글 포함)
```

**입력**: PDF 파일
**출력**: 순수 텍스트 (UTF-8)
**예시**:
```
Java는 객체지향 프로그래밍 언어입니다.
JVM 위에서 실행되므로 플랫폼 독립적입니다.
```

#### 2단계: 청킹

```
원본 텍스트
├─ 토큰화 (GPT-2 토크나이저)
├─ 512 토큰 크기로 분할
├─ 128 토큰 오버랩 (이전 청크와 다음 청크 겹침)
└─ 문단 경계 존중 (가능한 한 문단의 끝에서 분할)

예시:
청크 0: [토큰 0-511]      (내용: "Java의 기본...")
청크 1: [토큰 384-895]    (오버랩 128 토큰: 토큰 384-511)
청크 2: [토큰 768-1279]   (오버랩 128 토큰: 토큰 768-895)
```

**데이터 구조**:
```java
public class DocumentChunk {
    private Long id;
    private Long documentId;
    private String text;              // 청크 텍스트
    private Vector embedding;         // pgvector(1536)
    private int chunkIndex;           // 순서
    private LocalDateTime createdAt;
}
```

#### 3단계: 임베딩 생성

```
청크별 OpenAI API 호출

Request:
{
  "model": "text-embedding-3-small",
  "input": [
    "Java는 객체지향 프로그래밍 언어입니다...",
    "Python은 동적 타입 언어입니다...",
    ...  (최대 100개)
  ]
}

Response:
{
  "data": [
    {
      "index": 0,
      "embedding": [0.001, -0.002, 0.003, ...]  // 1536차원
    },
    ...
  ]
}

저장:
INSERT INTO DOCUMENT_CHUNKS
(document_id, text, embedding, chunk_index)
VALUES
(1, "Java는...", '[0.001, -0.002, ...]'::vector(1536), 0)
```

**비용 계산**:
- 요청당 비용: $0.02 / 1M tokens
- 100개 청크 (평균 500 토큰): $0.001

#### 4단계: pgvector 저장 및 인덱싱

```sql
-- HNSW 인덱스 자동 생성
CREATE INDEX ON document_chunks USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);

-- 코사인 유사도 검색 예시
SELECT chunk_index, text, (1 - (embedding <=> query_embedding)) as similarity
FROM document_chunks
WHERE document_id = 1
ORDER BY embedding <=> query_embedding
LIMIT 5;

-- <=> : 코사인 유사도 거리 연산자
-- (1 - distance) = 유사도 (0-1 범위)
```

**인덱스 파라미터**:
- `m = 16`: 각 노드의 최대 연결 수 (메모리/성능 트레이드오프)
- `ef_construction = 64`: 구성 시 탐색 깊이 (값이 클수록 정확하지만 느림)

#### 5단계: 질문 생성

```
각 청크마다 OpenAI GPT 호출

System Prompt:
"다음 텍스트를 기반으로 선택지 5개가 있는 객관식 질문 1개를 생성하세요.
정답은 a, b, c, d, e 중 하나입니다.
JSON 형식으로 응답하세요."

User Prompt:
"텍스트: Java는 객체지향 프로그래밍 언어입니다.
JVM 위에서 실행되므로 플랫폼 독립적입니다."

Response:
{
  "question": "Java의 주요 특징은?",
  "options": [
    "a) 플랫폼 독립적이다",
    "b) 정적 타입 언어이다",
    "c) 메모리 자동 관리",
    "d) 함수형 프로그래밍만 지원",
    "e) C보다 빠르다"
  ],
  "correctAnswer": "a",
  "difficulty": "EASY",
  "explanation": "JVM의 WORA(Write Once, Run Anywhere) 철학으로..."
}
```

#### 6단계: 질문 저장

```java
public class Question {
    private Long id;
    private Long documentId;
    private QuestionType type;        // MULTIPLE_CHOICE, SHORT_ANSWER, ESSAY
    private String content;
    private List<QuestionOption> options;  // 5개
    private String correctAnswer;
    private String explanation;
    private QuestionDifficulty difficulty; // EASY, MEDIUM, HARD
    private LocalDateTime createdAt;
}

public class QuestionOption {
    private Long id;
    private Long questionId;
    private String text;
    private char optionLabel;  // a, b, c, d, e
}
```

**저장 쿼리**:
```sql
INSERT INTO QUESTIONS
(document_id, type, content, correct_answer, difficulty, explanation)
VALUES (1, 'MULTIPLE_CHOICE', 'Java의 주요 특징은?', 'a', 'EASY', '...');

INSERT INTO QUESTION_OPTIONS
(question_id, text, option_label)
VALUES
(1, '플랫폼 독립적이다', 'a'),
(1, '정적 타입 언어이다', 'b'),
(1, '메모리 자동 관리', 'c'),
(1, '함수형 프로그래밍만 지원', 'd'),
(1, 'C보다 빠르다', 'e');
```

### 문서 처리 타이밍

```
상태 전환:
UPLOADED (파일 업로드)
  ↓ (즉시)
PROCESSING (처리 시작)
  ↓ (1-5분, 파일 크기에 따라)
COMPLETED (성공)
또는
FAILED (오류 발생)
```

**타임아웃**: 파일이 30분 이상 PROCESSING 상태면 자동 FAILED로 변경 (배치 작업)

## 2. 시험 생성 및 진행 흐름

### 시험 생성 (CreateExam)

```
클라이언트
  │
  ▼
[POST /api/v1/exams]
{
  "documentIds": [1, 2, 3],
  "questionCount": 10,
  "timeLimit": 3600
}
  │
  ▼
[ExamController]
  │ DTO 검증
  ▼
[CreateExamUseCase]
  │
  ├─ 1. 선택된 문서에서 질문 조회
  │    QuestionRepository.findByDocumentIdIn([1,2,3])
  │
  ├─ 2. 시험 생성 규칙 검증
  │    - 총 문제 수 >= 10개?
  │    - EASY >= 1, MEDIUM >= 1, HARD >= 1?
  │    - 요청한 개수 >= 실제 가능한 개수?
  │
  ├─ 3. 질문 선택 (난이도 분포 고려)
  │    - EASY: 40% (4개)
  │    - MEDIUM: 40% (4개)
  │    - HARD: 20% (2개)
  │
  ├─ 4. Exam 엔티티 생성
  │    - status: CREATED
  │    - startedAt: NOW()
  │    - expiresAt: NOW() + timeLimit
  │
  └─ 5. 질문 관계 설정
       exam.questionIds = [q1, q3, q5, q7, q9, q12, q15, q18, q21, q24]
  │
  ▼
PostgreSQL에 저장
  │ (exam row)
  │ (exam_question 매핑)
  ▼
응답: ExamCreatedResult
{
  "examId": 5,
  "status": "CREATED",
  "questions": [...]
  "startedAt": "2025-03-12T14:00:00Z",
  "expiresAt": "2025-03-12T15:00:00Z"
}
```

### 시험 진행 (SaveAnswer)

```
클라이언트 (브라우저/앱)
  │
  ├─ 질문 1 답안: "a"
  │  POST /api/v1/exams/{examId}/answers
  │  { "questionId": 1, "answer": "a" }
  │  ▼
  │  [ExamController]
  │  ▼
  │  [SaveAnswerUseCase]
  │  ▼
  │  [ExamAnswerCache (Redis)]
  │  KEY: exam:5:q1 = "a"
  │  TTL: 시험 종료까지 (1시간)
  │  ▼
  │  응답: 200 OK
  │
  ├─ 질문 2 답안: "c"
  │  (동일 흐름)
  │  KEY: exam:5:q2 = "c"
  │
  ├─ 질문 3 스킵 (답변 안 함)
  │  (저장하지 않음)
  │
  └─ ... (반복)

Redis 상태:
{
  "exam:5:q1": "a",
  "exam:5:q2": "c",
  "exam:5:q4": "b",
  "exam:5:q6": "d",
  ...
}
```

**왜 Redis?**
- 빠른 응답 (1ms 이하)
- 자동 만료 (시험 끝나면 자동 삭제)
- 데이터베이스 부하 감소

### 시험 제출 (SubmitExam)

```
클라이언트
  │
  ▼
[POST /api/v1/exams/{examId}/submit]
  │
  ▼
[ExamController]
  │
  ▼
[SubmitExamUseCase]
  │
  ├─ 1. 시험 조회
  │    Exam exam = ExamRepository.findById(5)
  │
  ├─ 2. 시간 초과 확인
  │    if (NOW() > exam.expiresAt) {
  │      자동 채점 (현재까지의 답안)
  │    }
  │
  ├─ 3. Redis에서 모든 답안 조회
  │    Map<Integer, String> answers =
  │      Redis.getAll("exam:5:*")
  │    // exam:5:q1 → "a"
  │    // exam:5:q2 → "c"
  │
  ├─ 4. 정답 조회
  │    List<Question> questions =
  │      QuestionRepository.findByIdIn([q1,q2,...])
  │    // q1.correctAnswer = "a"
  │    // q2.correctAnswer = "a" (사용자는 "c")
  │
  ├─ 5. 채점 (ExamGradingService)
  │    score = 0
  │    for each question:
  │      if (userAnswer == correctAnswer):
  │        score += (100 / totalQuestions)
  │    // 7개 맞음 → score = 70
  │
  ├─ 6. 결과 계산
  │    grade = calculateGrade(score)  // C+
  │    passed = score >= 50  // true
  │
  ├─ 7. 답안을 DB에 저장
  │    INSERT INTO ANSWERS VALUES (...)
  │    // 변경 불가능한 기록 보존
  │
  ├─ 8. Exam 상태 업데이트
  │    exam.status = SUBMITTED
  │    exam.submittedAt = NOW()
  │    exam.score = 70
  │    exam.grade = "C+"
  │
  ├─ 9. Redis 캐시 삭제
  │    Redis.delete("exam:5:*")
  │
  └─ 10. 응답 반환
  │
  ▼
응답: SubmitResult
{
  "examId": 5,
  "status": "SUBMITTED",
  "totalQuestions": 10,
  "correctAnswers": 7,
  "score": 70,
  "grade": "C+",
  "passed": true,
  "detailedResults": [
    {
      "questionId": 1,
      "userAnswer": "a",
      "correctAnswer": "a",
      "correct": true
    },
    {
      "questionId": 2,
      "userAnswer": "c",
      "correctAnswer": "a",
      "correct": false
    },
    ...
  ]
}
```

## 3. 인증 흐름

### 로그인 시퀀스

```
클라이언트
  │
  ▼
[1. 로그인 요청]
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
  │
  ▼
[2. AuthController.login()]
  │
  ├─ DTO 검증 (@Valid)
  │
  ├─ LoginUseCase.execute(command)
  │
  ├─ UserRepository.findByEmail("user@example.com")
  │
  └─ 조회 결과: User 엔티티
      {
        id: 1,
        email: "user@example.com",
        password: "$2a$10$...",  // bcrypt 해시
        name: "홍길동",
        provider: "LOCAL"
      }
  │
  ▼
[3. 비밀번호 검증]
  │ BCryptPasswordEncoder.matches(
  │   rawPassword: "SecurePassword123!",
  │   encodedPassword: "$2a$10$..."
  │ )
  │ ▼ 일치함
  │
  ▼
[4. JwtTokenProvider.generateTokens()]
  │
  ├─ Access Token 생성
  │   - 알고리즘: HS256
  │   - 유효기간: 1시간
  │   - Claims: userId, email, role
  │   - Header: { "alg": "HS256", "typ": "JWT" }
  │   - Payload: { "sub": "1", "exp": 1234567890, ... }
  │   - Signature: HMACSHA256(header.payload, secret_key)
  │
  └─ Refresh Token 생성
      - 랜덤 UUID
      - Redis에 저장
      - KEY: "refresh:user_1"
      - VALUE: { "token": "...", "expiresAt": ... }
      - TTL: 7일
  │
  ▼
[5. 응답 생성]
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IlRlc3QiLCJpYXQiOjE1MTYyMzkwMjJ9.xxxx",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
  │
  ▼
클라이언트
```

### 토큰 검증 (protected 요청)

```
클라이언트
  │
  ▼
[요청 생성]
GET /api/v1/exams
Headers: {
  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
  │
  ▼
[JwtAuthenticationFilter (Spring Security)]
  │
  ├─ 1. 헤더에서 토큰 추출
  │    String authHeader = "Bearer eyJhbGc..."
  │    String token = authHeader.substring(7)
  │
  ├─ 2. JwtTokenProvider.validateToken(token)
  │    a) 서명 검증 (HMACSHA256)
  │    b) 만료 시간 확인 (exp claim)
  │    c) 필수 클레임 확인 (sub, exp, iat)
  │
  ├─ 3. 토큰이 블랙리스트에 없는지 확인 (로그아웃 확인)
  │    if (Redis.exists("blacklist:" + token)) {
  │      throw new InvalidTokenException()
  │    }
  │
  ├─ 4. 사용자 정보 추출
  │    Long userId = token.getSubject()
  │    UserDetailsServiceImpl.loadUserByUsername(userId)
  │
  ├─ 5. SecurityContext에 인증 정보 설정
  │    Authentication auth = new UsernamePasswordAuthenticationToken(
  │      userDetails,
  │      null,
  │      userDetails.getAuthorities()
  │    )
  │    SecurityContext.setAuthentication(auth)
  │
  └─ 6. 다음 필터로 이동
  │
  ▼
[Controller]
  │ @AuthenticationPrincipal CustomUserDetails userDetails로
  │ 현재 사용자 정보 접근 가능
  │
  ▼
응답
```

### JWT 토큰 구조

```
Access Token:
Header.Payload.Signature

Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "1",           // userId
  "email": "user@example.com",
  "name": "홍길동",
  "iat": 1234567890,    // issued at
  "exp": 1234571490,    // expires (1시간)
  "iss": "tmk-api"
}

Signature:
HMACSHA256(
  base64(Header) + "." + base64(Payload),
  secret_key
)
```

## 4. Redis 캐시 전략

### 캐시 키 설계

```
인증코드:
  - KEY: "email-verification:{email}"
  - VALUE: { "code": "123456", "attempts": 1 }
  - TTL: 5분

Refresh Token:
  - KEY: "refresh:{userId}"
  - VALUE: { "token": "uuid", "ip": "192.168.1.1" }
  - TTL: 7일

시험 진행 중 답안:
  - KEY: "exam:{examId}:q{questionId}"
  - VALUE: "사용자 답안"
  - TTL: 시험 종료까지 (동적)

시험 진행 상태:
  - KEY: "exam:{examId}:state"
  - VALUE: { "currentQuestion": 3, "startedAt": ... }
  - TTL: 시험 종료까지

사용자 정보 캐시:
  - KEY: "user:{userId}"
  - VALUE: User DTO
  - TTL: 30분

토큰 블랙리스트 (로그아웃):
  - KEY: "blacklist:{token}"
  - VALUE: "true"
  - TTL: 토큰 만료까지
```

### 캐시 무효화

```
이벤트별 캐시 정리:

회원가입 완료:
  - Redis.delete("email-verification:{email}")

로그인:
  - Redis.set("refresh:{userId}", newToken)

시험 제출:
  - Redis.delete("exam:{examId}:*")
  - Redis.delete("exam:{examId}:state")

로그아웃:
  - Redis.set("blacklist:{accessToken}", true)
  - Redis.set("blacklist:{refreshToken}", true)
  - Redis.delete("refresh:{userId}")
```

## 5. 배치 작업 데이터 흐름

### ExamAutoSubmitJob (매 1분)

```
[Spring Scheduler] (매 1분)
  │
  ▼
[ExamAutoSubmitJobConfig.scheduleExamAutoSubmitJob()]
  │
  ▼
[ExamAutoSubmitJob 시작]
  │
  ▼
[ExamAutoSubmitReader]
  │
  └─ 쿼리: SELECT * FROM EXAMS
            WHERE status = 'IN_PROGRESS'
            AND started_at + INTERVAL 1 hour < NOW()
  │
  ▼
[결과 예시]
{
  examId: 3,
  userId: 2,
  status: "IN_PROGRESS",
  startedAt: "2025-03-12 14:00:00",
  questionIds: [1, 3, 5, 7, 9, 12, 15, 18, 21, 24],
  expiresAt: "2025-03-12 15:00:00"  // NOW()보다 이전
}
  │ (배치: 100개씩)
  ▼
[ExamAutoSubmitProcessor]
  │
  ├─ 1. Redis에서 답안 조회
  │    for questionId in questionIds:
  │      answer = Redis.get("exam:3:q{questionId}")
  │      // "a", "c", null, "b", ...
  │
  ├─ 2. ExamGradingService.grade()
  │    - 정답 조회
  │    - 점수 계산
  │    - 등급 부여
  │
  ├─ 3. ExamSubmitResult 객체 생성
  │    {
  │      examId: 3,
  │      userId: 2,
  │      totalQuestions: 10,
  │      correctAnswers: 6,
  │      score: 60,
  │      grade: "D+",
  │      passed: false,
  │      answers: [...]
  │    }
  │
  └─ 4. 다음 단계로 전달
  │
  ▼
[ExamAutoSubmitWriter]
  │
  ├─ 1. Exam 상태 업데이트
  │    UPDATE EXAMS SET status='SUBMITTED', score=60, grade='D+', submitted_at=NOW()
  │    WHERE id = 3
  │
  ├─ 2. ANSWERS 테이블 저장
  │    INSERT INTO ANSWERS (exam_id, question_id, user_answer, is_correct)
  │    VALUES (3, 1, 'a', true),
  │           (3, 3, 'c', false),
  │           ...
  │
  ├─ 3. Redis 캐시 삭제
  │    Redis.delete("exam:3:*")
  │
  └─ 4. 로그 기록
       logger.info("Exam 3 auto-submitted: score=60, grade=D+")
  │
  ▼
[다음 배치 아이템 처리]
```

### ExpiredVerificationCleanJob (매일 02:00 AM)

```
[Spring Scheduler] (매일 02:00)
  │
  ▼
[ExpiredVerificationCleanJob 시작]
  │
  ▼
[VerificationCleanReader]
  │
  └─ 쿼리: SELECT * FROM EMAIL_VERIFICATIONS
            WHERE status IN ('PENDING', 'EXPIRED')
            AND created_at < NOW() - INTERVAL 1 day
  │
  ▼
[결과 예시]
{
  id: 1,
  email: "old@example.com",
  status: "EXPIRED",
  createdAt: "2025-03-10 10:00:00"
},
{
  id: 2,
  email: "pending@example.com",
  status: "PENDING",
  createdAt: "2025-03-10 15:00:00"
}
  │ (배치: 1000개씩)
  ▼
[VerificationCleanProcessor]
  │
  ├─ 각 항목 검증
  │   - 이미 삭제되지 않았는지 확인
  │   - 해당 사용자가 이미 가입했는지 확인
  │
  └─ 삭제 대상 필터링
  │
  ▼
[VerificationCleanWriter]
  │
  ├─ 1. DB에서 삭제
  │    DELETE FROM EMAIL_VERIFICATIONS WHERE id IN (1, 2, ...)
  │
  ├─ 2. Redis 캐시 정리
  │    Redis.delete("email-verification:{email}")
  │
  └─ 3. 로그 기록
       logger.info("Cleaned 50 expired verifications")
  │
  ▼
[완료]
```

## 6. 의존성 데이터 흐름

### 문서 → 질문 → 시험

```
Document (id=1)
  │ 포함
  ├─ DocumentChunk (id=1-50) with embeddings
  │  │
  │  ▼ 생성
  │  Question (id=1-42)
  │    ├─ QuestionOption (a, b, c, d, e)
  │    ├─ Difficulty: EASY (14개), MEDIUM (14개), HARD (14개)
  │    └─ Type: MULTIPLE_CHOICE
  │
  ├─ Question (id=1-42)
  │  │
  │  └─ ▼ 선택되어
  │    Exam (id=5)
  │      └─ exam_question (question_id IN [1, 5, 9, 12, ...])
  │

Document 삭제 시 영향:
Document → DeleteCascade → DocumentChunk (임베딩 벡터 삭제)
                        → Question (삭제, 진행 중인 시험 영향 없음)
```

## 7. 데이터 일관성 보장

### 트랜잭션 경계

```
Document 처리 (비동기 배치):
- 트랜잭션 단위: 청크별
- 실패 시: 해당 청크만 재시도
- 롤백 대상: DOCUMENT_CHUNKS, QUESTIONS

시험 제출 (동기):
- 트랜잭션 단위: 전체 시험 채점
- 실패 시: 전체 롤백 (답안 미저장)
- 롤백 대상: EXAMS, ANSWERS

토큰 생성 (동기):
- Redis에만 저장 (원자적)
- 데이터베이스 트랜잭션 불필요
```

### 분산 일관성 전략

```
Event-Driven 패턴 (미래 확장):

시험 제출 완료 이벤트
  ↓
ExamSubmittedEvent 발행
  ├─ Handler 1: 결과 저장 (DB)
  ├─ Handler 2: 사용자 알림 발송
  ├─ Handler 3: 분석 데이터 수집
  └─ Handler 4: 캐시 무효화

이벤트 저널:
- 모든 도메인 이벤트 기록
- 이벤트 순서 보장
- 실패한 이벤트 재시도 가능
```

## 8. 성능 최적화 포인트

### 인덱스 전략

```
Documents:
- PK: id
- FK: user_id
- 인덱스: user_id, status, created_at

Questions:
- PK: id
- FK: document_id
- 인덱스: document_id, type, difficulty

Exams:
- PK: id
- FK: user_id
- 인덱스: user_id, status, created_at
- 인덱스: started_at + INTERVAL 1 hour (배치 조회용)

DocumentChunks:
- PK: id
- FK: document_id
- Vector Index: HNSW (embedding)
- 인덱스: document_id, chunk_index

EmailVerifications:
- PK: id
- 인덱스: email, status
- 인덱스: created_at (정리용)
```

### 쿼리 최적화

```
N+1 쿼리 방지:
- exam.getQuestions() → Fetch Join 사용
  SELECT e FROM Exam e
  JOIN FETCH e.questions
  WHERE e.id = ?

배치 조회:
- ExamAutoSubmitJob의 Reader
  배치 크기: 100개
  Chunk: true (메모리 효율)

캐시 레이어:
- 자주 조회되는 데이터 캐싱
  - User (30분)
  - Question (1시간)
  - Document 메타데이터 (1시간)
```
