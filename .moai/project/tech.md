---
title: TMK 기술 스택
description: 기술 선택 사항 및 아키텍처 결정 이유
---

# TMK 기술 스택 및 아키텍처

## 기술 스택 개요

### 프로그래밍 언어 및 런타임

| 항목 | 버전 | 선택 이유 |
|------|------|----------|
| **Java** | 21 LTS | 모던 Java 기능 (레코드, 패턴 매칭, 가상 스레드), LTS 지원 기간(2031년까지) |
| **Gradle** | 8.5+ | 멀티 모듈 관리 우수, 빌드 성능, DSL 언어의 표현력 |

### 웹 프레임워크 및 보안

| 항목 | 버전 | 선택 이유 |
|------|------|----------|
| **Spring Boot** | 3.5.11 | 최신 안정 버전, Spring 6.x 지원, 빠른 개발, 풍부한 스타터 |
| **Spring Security** | 6.x | Spring Boot 3.5 기본 포함, JWT 지원, 인증/인가 표준화 |
| **Spring Web** | 6.x | RESTful API 개발 표준, @RestController, @RequestMapping |
| **JJWT** | 0.12.6 | JWT 토큰 생성/검증, RS256 서명 알고리즘 지원 |

### 데이터 접근 및 ORM

| 항목 | 버전 | 선택 이유 |
|------|------|----------|
| **Spring Data JPA** | Spring Boot 3.5 | 쿼리 메서드 자동 생성, 페이징/정렬, 트랜잭션 관리 |
| **Jakarta Persistence** | 3.1 | javax → jakarta 전환, 최신 표준 준수 |
| **PostgreSQL Driver** | 42.x | 오픈소스, 안정성, pgvector 확장 지원 |

### 데이터베이스 및 벡터 저장소

| 항목 | 버전 | 선택 이유 |
|------|------|----------|
| **PostgreSQL** | 14+ | ACID 준수, pgvector 확장 지원, JSON/JSONB, 신뢰성 |
| **pgvector** | 0.5.0+ | PostgreSQL 벡터 저장, ANN(근사 최근접) 검색, 확장성 |
| **Redis** | 7.x | 고속 캐시, TTL 기반 자동 삭제, 세션 저장소 |

### 배치 처리 및 스케줄링

| 항목 | 버전 | 선택 이유 |
|------|------|----------|
| **Spring Batch** | Spring Boot 3.5 | 대량 데이터 처리, ItemReader/Processor/Writer 아키텍처 |
| **Spring Scheduler** | Spring Boot 3.5 | 간단한 배치 스케줄링, @Scheduled 애너테이션 |
| **Quartz** | 2.3.2 | (선택사항) 복잡한 스케줄링, 클러스터링, 영구 저장 |

### AI 및 머신러닝 통합

| 항목 | 버전 | 선택 이유 |
|------|------|----------|
| **OpenAI API** | text-embedding-3-small | 임베딩 생성, 1536차원, 저비용 |
| **OpenAI API** | GPT-4 / GPT-3.5 | 문제 생성, 다양한 질문 유형 지원 |

### 유틸리티 및 개발도구

| 항목 | 버전 | 선택 이유 |
|------|------|----------|
| **Lombok** | 1.18.30+ | 보일러플레이트 제거 (@Getter, @Setter, @AllArgsConstructor) |
| **Jakarta Persistence** | 3.1 | 표준 ORM 애너테이션 (@Entity, @Column) |
| **Validation** | Jakarta Bean Validation | 입력값 검증 (@NotNull, @Email) |

---

## 프레임워크 선택 사유

### 1. Spring Boot 3.5.11 선택 이유

**비교 옵션**: Quarkus, Micronaut, Play Framework

**선택 사유**:
- **생산성**: 자동 설정, 스타터, 빠른 프로토타이핑
- **생태계**: Spring Data JPA, Security, Batch 통합
- **커뮤니티**: 가장 많은 한국 개발자, 풍부한 학습 자료
- **성숙도**: 10년 이상의 프로덕션 검증
- **LTS 지원**: 3.5.x는 2026년까지 지원

**성능 트레이드오프**:
- Quarkus와 비교 시 약간의 메모리 오버헤드 (수용 가능)
- 하지만 개발 속도와 안정성으로 충분히 보상

### 2. PostgreSQL + pgvector 선택 이유

**비교 옵션**: MySQL, MongoDB, Elasticsearch, Pinecone

**선택 사유**:
- **통합성**: 관계형 데이터(사용자, 시험) + 벡터 데이터(임베딩) 통합 가능
- **비용**: 오픈소스, 호스팅 비용 저렴
- **성능**: HNSW 인덱스로 백만 건 이상 벡터 검색 < 100ms
- **신뢰성**: ACID 준수, 백업/복구 기능 우수
- **유연성**: JSON, Full-text Search, PostGIS 등 추가 기능

**대안과의 비교**:

| 항목 | PostgreSQL+pgvector | Pinecone | Elasticsearch |
|------|---------------------|----------|---------------|
| 월간 비용 | $0-100 (자체 호스팅) | $100+ (벡터만) | $500+ |
| 셋업 시간 | 30분 | 5분 | 1시간 |
| 학습곡선 | 낮음 | 매우 낮음 | 중간 |
| 벡터 검색 성능 | 우수 | 최고 | 우수 |
| 트랜잭션 지원 | ✅ | ❌ | 제한적 |
| 자체 호스팅 | ✅ | ❌ | ✅ |

**선택 결정**: 비용, 유연성, 학습곡선을 종합하면 PostgreSQL+pgvector가 MVP 단계에 최적

### 3. JWT 기반 인증 선택 이유

**비교 옵션**: OAuth 2.0, Session, API Key

**선택 사유**:
- **무상태성**: 서버 세션 저장 불필요, 수평 확장 용이
- **모바일 친화**: 앱에서 Bearer 토큰 관리 간단
- **마이크로서비스 준비**: 향후 모듈 분리 시 토큰 검증만으로 인증 가능
- **표준**: JWT는 업계 표준, 다양한 클라이언트 호환

**토큰 관리 전략**:
- **AccessToken**: 짧은 유효기간 (15분), 응답에 포함
- **RefreshToken**: 긴 유효기간 (7일), Redis에 저장, 보안 강화

---

## 데이터베이스 설계

### PostgreSQL + pgvector 아키텍처

#### 테이블 구조

```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL,  -- LOCAL, GOOGLE, KAKAO, NAVER
    provider_id VARCHAR(255),
    password_hash VARCHAR(255),
    nickname VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 문서 테이블 (학습 자료)
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status VARCHAR(50) NOT NULL,  -- REGISTERED, PROCESSING, COMPLETED, FAILED
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 문서 청크 (벡터화된 텍스트 단위)
CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id),
    text TEXT NOT NULL,
    embedding vector(1536),  -- OpenAI text-embedding-3-small
    chunk_index INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id)
);

-- HNSW 인덱스 (빠른 ANN 검색)
CREATE INDEX ON document_chunks
USING hnsw (embedding vector_cosine_ops)
WITH (m=16, ef_construction=64);

-- 문제 테이블
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id),
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,  -- MULTIPLE_CHOICE, SHORT_ANSWER, TRUE_FALSE
    difficulty VARCHAR(50) NOT NULL,  -- EASY, MEDIUM, HARD
    correct_answer VARCHAR(1000),
    explanation TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id)
);

-- 문제 보기 (객관식만)
CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    option_text VARCHAR(1000) NOT NULL,
    option_index INT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- 시험 테이블
CREATE TABLE exams (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,  -- IN_PROGRESS, SUBMITTED, GRADED
    total_score INT,
    correct_count INT,
    total_count INT,
    pass_yn CHAR(1),
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 시험-문제 매핑
CREATE TABLE exam_questions (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL REFERENCES exams(id),
    question_id BIGINT NOT NULL REFERENCES questions(id),
    question_order INT NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- 답안 테이블
CREATE TABLE exam_answers (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL REFERENCES exams(id),
    question_id BIGINT NOT NULL REFERENCES questions(id),
    user_answer VARCHAR(1000),
    is_correct CHAR(1),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- 이메일 인증 코드 (Redis에도 저장)
CREATE TABLE email_verifications (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    verified_yn CHAR(1) DEFAULT 'N',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMPTZ
);
```

#### 벡터 검색 쿼리 예시

```sql
-- 주어진 문제 관련 컨텍스트 검색 (코사인 유사도)
SELECT
    dc.id,
    dc.text,
    dc.embedding <=> $1::vector AS distance  -- 코사인 거리 (작을수록 유사)
FROM document_chunks dc
WHERE dc.document_id = $2
ORDER BY dc.embedding <=> $1::vector
LIMIT 5;  -- 상위 5개 청크 반환

-- HNSW 인덱스 활용으로 O(log N) 시간 복잡도, 백만 건 데이터도 < 50ms
```

#### 인덱스 전략

| 인덱스 | 테이블 | 열 | 사유 |
|--------|--------|-----|------|
| PRIMARY | 모든 테이블 | id | 기본 키 |
| UNIQUE | users | email | 중복 가입 방지 |
| UNIQUE | email_verifications | code | 인증 코드 유일성 |
| FOREIGN KEY | documents | user_id | 사용자별 문서 조회 |
| FOREIGN KEY | questions | document_id | 문서별 문제 조회 |
| FOREIGN KEY | exams | user_id | 사용자별 시험 조회 |
| HNSW | document_chunks | embedding | 벡터 유사도 검색 (핵심!) |

---

## Redis 설계

### Redis 키 전략

```
# 이메일 인증 코드
verification:{email}:{code}  →  ttl=300초(5분)
  { verified: false, created_at: timestamp }

# JWT RefreshToken
refresh_token:{userId}  →  ttl=604800초(7일)
  { token: token_value, issued_at: timestamp }

# 시험 진행 중 임시 답안
exam_answer:{examId}:{questionId}  →  ttl=3600초(1시간)
  { answer: value, saved_at: timestamp }

# 사용자 세션 (선택사항)
session:{sessionId}  →  ttl=1800초(30분)
  { userId: value, ip: value, user_agent: value }

# 캐시: 문제 목록 (자주 조회되는 데이터)
questions:document:{documentId}  →  ttl=3600초(1시간)
  [ { id, text, type, difficulty, ... } ]
```

### Redis 용도별 설정

| 용도 | TTL | 이유 |
|------|-----|------|
| 이메일 인증 코드 | 5분 | 보안 강화, 무작위 공격 방지 |
| RefreshToken | 7일 | 자동 로그아웃, 토큰 좌표화 |
| 시험 임시 답안 | 1시간 | 네트워크 장애 시 복구 용이 |
| 일반 캐시 | 1시간 | 데이터베이스 부하 감소 |
| 세션 | 30분 | 무상태 API지만 임시 상태 저장 |

### Redis 호출 패턴

```java
// 이메일 인증 코드 저장 (5분 유효)
redisTemplate.opsForValue()
    .set("verification:" + email + ":" + code,
         "{verified: false}",
         Duration.ofMinutes(5));

// RefreshToken 저장 (7일 유효)
redisTemplate.opsForValue()
    .set("refresh_token:" + userId,
         tokenValue,
         Duration.ofDays(7));

// 임시 답안 저장 (1시간, 네트워크 장애 복구)
redisTemplate.opsForValue()
    .set("exam_answer:" + examId + ":" + questionId,
         answerValue,
         Duration.ofHours(1));
```

---

## AI/ML 통합 설계

### OpenAI API 활용

#### 1. 임베딩 생성 (text-embedding-3-small)

```
입력: 문서 청크 (텍스트 512-1024 토큰)
         ↓
OpenAI API 호출
         ↓
출력: 1536차원 벡터
```

**선택 이유**:
- **비용**: text-embedding-3-large 대비 1/8 비용
- **성능**: 대부분 작업에서 충분한 품질
- **속도**: 빠른 응답 시간
- **대안**: text-embedding-3-large (더 정확, 3배 비용)

**호출 비용**:
- 1M 토큰당 $0.02 (text-embedding-3-small)
- 월 1M 문서 처리 시 약 $20

**구현 예시**:

```java
// OpenAI API 호출
OpenAIService openAiService = new OpenAIService(apiKey);

EmbeddingRequest request = EmbeddingRequest.builder()
    .model("text-embedding-3-small")
    .input(text)
    .build();

EmbeddingResult result = openAiService.createEmbeddings(request);
float[] embedding = result.getData().get(0).getEmbedding()
    .stream()
    .map(BigDecimal::floatValue)
    .toArray();

// PostgreSQL pgvector에 저장
documentChunkRepository.save(new DocumentChunk(
    documentId, text, embedding, chunkIndex));
```

#### 2. 문제 생성 (GPT-4 / GPT-3.5)

```
입력: 문서 청크 3-5개 (RAG에서 검색된 관련 컨텍스트)
       + 프롬프트: "다음 텍스트에서 객관식 문제를 생성하세요"
         ↓
OpenAI API 호출 (gpt-4-turbo 또는 gpt-3.5-turbo)
         ↓
출력: {
  "type": "MULTIPLE_CHOICE",
  "question": "질문 텍스트",
  "options": ["보기1", "보기2", "보기3", "보기4", "보기5"],
  "correctAnswer": "보기1",
  "explanation": "해설"
}
```

**모델 선택**:

| 모델 | 비용 | 품질 | 속도 | 선택 |
|------|------|------|------|------|
| GPT-4 | $0.03/1K | 최고 | 느림 | ❌ (비용) |
| GPT-4 Turbo | $0.01/1K | 매우 높음 | 빠름 | ✅ (추천) |
| GPT-3.5 Turbo | $0.0005/1K | 높음 | 매우 빠름 | ✅ (비용) |
| Claude 3 (Anthropic) | $0.003/1K | 최고 | 중간 | 대안 |

**선택 결정**: MVP는 gpt-3.5-turbo로 시작, 품질 향상 필요 시 turbo로 전환

#### 3. RAG (Retrieval Augmented Generation) 플로우

```
사용자가 시험 응시 요청
    ↓
1. 시험 생성 (CreateExamUseCase)
    - 사용자의 모든 문서에서 문제 선택
    - 각 난이도(EASY/MEDIUM/HARD) 최소 1문제
    - 총 10문제 이상

2. 문제 충분하지 않은 경우, AI 문제 생성
    - 문서 ID 기반 벡터 검색
    - 각 청크당 관련 컨텍스트 top-5 추출
    - OpenAI에 컨텍스트 포함하여 문제 생성
    - 생성된 문제 검증 (형식, 난이도)

3. 시험 객체 생성
    - ExamCreationService.createExam(questionIds)
    - ExamQuestion 매핑
    - 상태: IN_PROGRESS

4. Redis에 시험 세션 저장
    - 시험 시간: 30분 (시작 시간 + 30분에 자동 제출)
    - 임시 답안 저장 공간 할당

5. API 응답
    - 시험 ID, 문제 목록, 남은 시간
```

---

## 보안 아키텍처

### 인증 흐름

```
1. 회원가입
   POST /api/v1/auth/register
   {
     "email": "user@example.com",
     "password": "encrypted",
     "nickname": "사용자"
   }
   → 사용자 저장 (password는 bcrypt로 해시)
   → 응답: accessToken + refreshToken

2. 로그인
   POST /api/v1/auth/login
   {
     "email": "user@example.com",
     "password": "user_password"
   }
   → 이메일 존재 확인
   → bcrypt 패스워드 검증
   → JWT accessToken 생성 (15분 유효)
   → JWT refreshToken 생성 (7일 유효) + Redis 저장
   → 응답: accessToken + refreshToken

3. 인증된 요청
   GET /api/v1/exams
   Header: Authorization: Bearer {accessToken}
   → SecurityFilterChain에서 JWT 검증
   → JwtAuthenticationFilter에서 토큰 파싱
   → UserDetails 로드
   → @PreAuthorize 권한 확인
   → 요청 처리

4. 토큰 만료 시 재발급
   POST /api/v1/auth/reissue
   Header: Authorization: Bearer {refreshToken}
   → RefreshToken 유효성 확인 (Redis)
   → 새 AccessToken 생성
   → 응답: 새 accessToken
```

### JWT 토큰 구조

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "userId",
    "email": "user@example.com",
    "iat": 1234567890,
    "exp": 1234569690,  // 15분 후
    "authorities": ["ROLE_USER"]
  },
  "signature": "..."
}
```

**토큰 검증**:
- 서명 확인: HMAC-SHA256 (JWT_SECRET 사용)
- 만료 시간 확인: exp 클레임
- 클레임 유효성: email, authorities 포함 여부

---

## 성능 최적화

### 데이터베이스 성능

#### 1. 벡터 검색 성능 (HNSW 인덱스)

```
쿼리: 주어진 벡터와 유사한 상위 5개 청크 검색

CREATE INDEX ON document_chunks
USING hnsw (embedding vector_cosine_ops)
WITH (m=16, ef_construction=64);

성능:
- 데이터 크기: 1M 벡터 (1536차원)
- 쿼리 응답: < 50ms
- 메모리: 약 2GB (1M개 벡터 기준)

HNSW 파라미터:
- m=16: 각 노드의 연결 수 (클수록 정확, 메모리 증가)
- ef_construction=64: 인덱스 생성 중 검색 범위 (클수록 정확, 시간 증가)
- ef_search: 쿼리 시 검색 범위 (기본 40, 클수록 정확하지만 느림)
```

#### 2. 쿼리 최적화

```sql
-- 나쁜 쿼리: N+1 문제
SELECT * FROM exams WHERE user_id = 1;
-- 각 시험마다 문제 조회: 추가 쿼리
SELECT * FROM exam_questions WHERE exam_id = ?;

-- 좋은 쿼리: LEFT JOIN
SELECT e.*, eq.*, q.*
FROM exams e
LEFT JOIN exam_questions eq ON e.id = eq.exam_id
LEFT JOIN questions q ON eq.question_id = q.id
WHERE e.user_id = 1;

-- JPA에서의 해결책: @EntityGraph
@EntityGraph(attributePaths = {"questions"})
List<Exam> findByUserId(Long userId);
```

#### 3. 인덱스 활용

| 쿼리 패턴 | 인덱스 | 예상 성능 |
|----------|--------|----------|
| WHERE user_id = ? | FOREIGN KEY | O(log N) |
| WHERE email = ? | UNIQUE | O(log N) |
| ORDER BY created_at DESC | 없음 | O(N log N) |
| 벡터 유사도 | HNSW | O(log N) |

### Redis 캐시 전략

```java
// 문제 목록 캐시 (1시간)
@Cacheable(value = "questions", key = "#documentId",
           unless = "#result == null", ttl = 3600)
public List<Question> getQuestionsByDocumentId(Long documentId) {
    return questionRepository.findByDocumentId(documentId);
}

// 캐시 무효화 (새 문제 생성 시)
@CacheEvict(value = "questions", key = "#documentId")
public void invalidateQuestionCache(Long documentId) {
}
```

### API 응답 시간 최적화

```
목표: 평균 응답 시간 < 200ms

분석:
- 데이터베이스 쿼리: 50-100ms (인덱스 활용)
- JPA 매핑: 10-20ms (관계 설정 최소화)
- JSON 직렬화: 10ms (jackson 활용)
- 네트워크: 10-50ms (클라이언트 위치 의존)
- 총합: 80-200ms
```

---

## 개발 환경 요구사항

### 시스템 요구사항

| 항목 | 요구사항 | 권장사항 |
|------|---------|---------|
| **JDK** | Java 21 이상 | Java 21.0.2 LTS |
| **메모리** | 4GB 이상 | 8GB 이상 |
| **디스크** | 2GB (프로젝트) | 10GB (개발용 전체) |
| **CPU** | 2코어 이상 | 4코어 이상 |
| **OS** | macOS, Linux, Windows | macOS 또는 Linux 권장 |

### 필수 로컬 서비스

#### PostgreSQL 설치 및 설정

```bash
# macOS (Homebrew)
brew install postgresql
brew services start postgresql

# 데이터베이스 생성
createdb tmk

# pgvector 확장 설치
psql tmk -c "CREATE EXTENSION IF NOT EXISTS vector;"

# 확인
psql tmk -c "SELECT extname FROM pg_extension WHERE extname = 'vector';"
```

#### Redis 설치 및 설정

```bash
# macOS (Homebrew)
brew install redis
brew services start redis

# 포트 확인
redis-cli ping  # PONG이 출력되면 정상
```

### 환경 변수 설정

```yaml
# application.yml 또는 환경변수

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tmk
    username: ${DB_USERNAME:-postgres}
    password: ${DB_PASSWORD:-password}

  jpa:
    hibernate:
      ddl-auto: validate  # 프로덕션: validate, 개발: update

  data:
    redis:
      host: localhost
      port: 6379

  mail:
    host: ${MAIL_HOST:-smtp.gmail.com}
    port: ${MAIL_PORT:-587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-3.5-turbo

jwt:
  secret: ${JWT_SECRET:-your-256-bit-secret-key}
  access-token-expiry: 900  # 15분
  refresh-token-expiry: 604800  # 7일

social:
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
  kakao:
    client-id: ${KAKAO_CLIENT_ID}
    client-secret: ${KAKAO_CLIENT_SECRET}
  naver:
    client-id: ${NAVER_CLIENT_ID}
    client-secret: ${NAVER_CLIENT_SECRET}
```

### IDE 설정

**IntelliJ IDEA 설정**:

```
Settings → Languages & Frameworks → Java
  - Project SDK: 21
  - Project language level: 21

Settings → Build, Execution, Deployment → Compiler → Java Compiler
  - Project bytecode version: 21

Settings → Editor → Code Style → Java
  - Formatter: Intellij IDEA (기본값)
  - Import order: com.*, java.*, javax.*, 기타 (기본값)
```

---

## 빌드 및 실행 명령어

### 빌드

```bash
# 전체 빌드 (모든 모듈)
./gradlew build

# API 모듈만 빌드
./gradlew :tmk-api:build

# 테스트 스킵
./gradlew build -x test
```

### 실행

```bash
# REST API 서버 실행
./gradlew :tmk-api:bootRun

# Batch 서버 실행 (별도 터미널)
./gradlew :tmk-batch:bootRun

# 특정 프로필로 실행
./gradlew :tmk-api:bootRun --args='--spring.profiles.active=dev'
```

### 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.tmk.api.controller.*Test"

# 테스트 커버리지 리포트
./gradlew test jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html

# 통합 테스트만 (기본: 단위 테스트)
./gradlew test -D junit.jupiter.execution.parallel.enabled=true
```

---

## 인프라 배포 구성 (향후)

### 프로덕션 환경 아키텍처

```
인터넷
  ↓
[로드 밸런서] (Nginx, AWS ALB)
  ↓
[API 서버] ×3 (tmk-api, 포트 8080)
  ├→ PostgreSQL (RDS, Multi-AZ)
  ├→ Redis (ElastiCache)
  └→ S3 (문서 저장소)

[Batch 서버] ×1 (tmk-batch, 포트 8081)
  ├→ PostgreSQL (위와 동일)
  └→ Quartz (클러스터링)

[모니터링]
  ├→ CloudWatch (AWS)
  ├→ Datadog (APM)
  └→ ELK (로깅)
```

### 배포 파이프라인

```
Git Push (main branch)
  ↓
GitHub Actions (CI/CD)
  ├→ 빌드 (./gradlew build)
  ├→ 테스트 (./gradlew test)
  ├→ 코드 분석 (SonarQube)
  └→ Docker 이미지 생성
  ↓
ECR (Elastic Container Registry)
  ↓
ECS (Elastic Container Service)
  └→ Blue/Green 배포 (무중단)
```

---

## 기술 결정 문서 (ADR)

### ADR-001: 언어 선택 - Java 21

**상태**: 승인됨
**선택**: Java 21 LTS
**근거**:
1. 모던 기능 (패턴 매칭, 레코드)
2. LTS 지원 (2031년까지)
3. 팀의 Java 숙련도
4. Spring Boot 완벽 지원

---

### ADR-002: 데이터베이스 - PostgreSQL + pgvector

**상태**: 승인됨
**선택**: PostgreSQL (관계형) + pgvector (벡터) 통합
**근거**:
1. ACID 준수
2. 관계형 + 벡터 통합 가능
3. 비용 효율성 (오픈소스)
4. ANN 검색 성능 (HNSW)
5. 자체 호스팅 가능

**대안 검토**:
- Pinecone: 비용 높음, 벡터만 가능
- Elasticsearch: 복잡도 높음
- MongoDB + Weaviate: 두 개 DB 관리 필요

---

### ADR-003: 인증 방식 - JWT

**상태**: 승인됨
**선택**: JWT (JSON Web Token)
**근거**:
1. 무상태성 (서버 세션 불필요)
2. 모바일 친화적
3. 마이크로서비스 준비
4. 업계 표준

**토큰 관리**:
- AccessToken: 짧은 유효기간 (15분)
- RefreshToken: 긴 유효기간 (7일), Redis 저장

---

**마지막 수정**: 2026년 3월
**문서 버전**: 1.0.0
**검토자**: 개발팀
