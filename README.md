# TMK (Test My Knowledge)

> AI 기반 문제은행 플랫폼 — PDF 문서를 등록하면 자동으로 문제를 생성하고, 시험을 통해 학습 이해도를 확인할 수 있는 서비스

---

## 주요 기능

- **AI 문제 자동 생성**: PDF 문서를 등록하면 OpenAI를 통해 객관식·빈칸 채우기·구현 문제를 자동 생성
- **RAG 기반 문제 생성**: 문서를 청킹·임베딩하여 pgvector에 저장, ANN 검색으로 관련 컨텍스트를 추출해 LLM에 전달
- **시험 응시**: 난이도별(쉬움·보통·어려움) 최소 1문제씩 포함된 10문제 이상의 시험을 생성하여 응시
- **자동 채점**: 시험 시간 만료 시 Spring Batch가 자동으로 제출 및 채점 처리
- **결과 분석**: 정답률 50% 이상 합격, 문제별 정답·해설 확인 가능
- **이메일 인증 회원가입 / 소셜 로그인**: Google, Kakao, Naver 소셜 로그인 지원

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5, Spring Batch |
| Security | Spring Security, JWT |
| Database | PostgreSQL + pgvector, Redis |
| AI | OpenAI API (text-embedding-3-small, GPT) |
| ORM | JPA / JPQL |
| Build | Gradle (멀티 모듈) |

---

## 아키텍처

클린 아키텍처 기반 멀티 모듈 구조로 설계되었습니다.

```
tmk-parent/
├── tmk-core/     # 순수 도메인 (Spring/JPA 등 외부 프레임워크 미의존)
│   ├── domain/   # Aggregate, Entity, Value Object, Enum
│   └── port/     # UseCase 인터페이스 (in), Repository 인터페이스 (out)
│
├── tmk-api/      # REST API, Spring Security, JWT, JPA 구현체
│   ├── controller/
│   ├── security/
│   └── adapter/  # in(Controller), out(JPA Adapter)
│
└── tmk-batch/    # Spring Batch
    └── job/
        ├── ExamAutoSubmitJob          # 매 1분: 만료 시험 자동 제출 및 채점
        └── ExpiredVerificationCleanJob # 매일 새벽: 만료 인증코드 삭제
```

**의존성 방향**: `tmk-api` → `tmk-core` ← `tmk-batch`

### 클린 아키텍처 준수 현황

| 항목 | 상태 | 내용 |
|------|------|------|
| 의존성 방향 | ✅ | 역방향 의존 없음. 빌드 레벨에서 강제 |
| 도메인 순수성 | ✅ | core Entity는 Jakarta Persistence + Lombok만 사용 |
| 도메인 로직 위치 | ✅ | `Exam.submit()`, `ExamQuestion.grade()` 등 Entity 내부 |
| Repository 추상화 | ✅ | 인터페이스는 core, JPA 구현체는 api에 분리 |
| 예외 처리 | ✅ | `BusinessException`, `ErrorCode`가 core에 위치 |
| core의 Spring 설정 | ⚠️ | `RedisConfig`, `JwtProvider` 등이 core에 위치 (api·batch 공유 목적의 실용적 선택) |

### 문서 처리 파이프라인

```
PDF 등록 → 텍스트 파싱 → 청킹 → OpenAI 임베딩(1536차원)
→ pgvector 저장 → ANN 검색(코사인 유사도) → LLM 문제 생성 → DB 저장
```

---

## 시작하기

### 사전 요구사항

- Java 21
- PostgreSQL (pgvector 확장 필요)
- Redis

### 환경 설정

PostgreSQL에 pgvector 확장을 활성화하세요.

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

`tmk-api/src/main/resources/application.yml`에 환경변수를 설정하세요.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tmk
    username: {DB_USERNAME}
    password: {DB_PASSWORD}
  data:
    redis:
      host: localhost
      port: 6379

openai:
  api-key: {OPENAI_API_KEY}

jwt:
  secret: {JWT_SECRET}
```

### 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew :tmk-api:bootRun

# 전체 테스트
./gradlew test
```

---

## API 개요

Base URL: `/api/v1`

| 도메인 | Method | URL | 설명 | 인증 |
|--------|--------|-----|------|------|
| Auth | POST | `/auth/send-verification` | 이메일 인증 코드 발송 | ❌ |
| Auth | POST | `/auth/verify` | 이메일 인증 코드 확인 | ❌ |
| Auth | POST | `/auth/register` | 회원가입 | ❌ |
| Auth | POST | `/auth/login` | 로그인 | ❌ |
| Auth | POST | `/auth/reissue` | 토큰 재발급 | ❌ |
| Auth | POST | `/auth/logout` | 로그아웃 | ✅ |
| Auth | GET | `/auth/social/{provider}` | 소셜 로그인 | ❌ |
| Question | GET | `/questions` | 문제 목록 조회 | ✅ |
| Question | GET | `/questions/{id}` | 문제 상세 조회 | ✅ |
| Exam | POST | `/exams` | 시험 생성 | ✅ |
| Exam | GET | `/exams/{id}` | 시험 문제 조회 | ✅ |
| Exam | PATCH | `/exams/{id}/answers` | 답안 저장/수정 | ✅ |
| Exam | POST | `/exams/{id}/submit` | 시험 제출 | ✅ |
| Exam | GET | `/exams/{id}/result` | 시험 결과 조회 | ✅ |
| Exam | GET | `/exams/history` | 시험 히스토리 목록 | ✅ |
| Exam | GET | `/exams/{id}/history` | 시험 히스토리 상세 | ✅ |

> 공통 응답 형식: `{ "errorCode": "SUCCESS", "msg": "ok", "data": {} }`
> 인증: `Authorization: Bearer {accessToken}`

자세한 API 명세는 [`docs/API 명세서.md`](docs/API%20명세서.md)를 참고하세요.

---

## 문서

| 문서 | 설명 |
|------|------|
| [`docs/TMK(Test My Knowledge).md`](docs/TMK(Test%20My%20Knowledge).md) | 프로젝트 개요, 요구사항, 구현 현황 |
| [`docs/기술 스택.md`](docs/기술%20스택.md) | 기술 선택 이유, 개발 환경 설정, 환경 변수 |
| [`docs/도메인 모델 설계.md`](docs/도메인%20모델%20설계.md) | 도메인 모델 및 비즈니스 규칙 |
| [`docs/API 명세서.md`](docs/API%20명세서.md) | 전체 API 엔드포인트 명세 |
| [`docs/ERD 설계.md`](docs/ERD%20설계.md) | 데이터베이스 ERD |
| [`docs/ddl.sql`](docs/ddl.sql) | 테이블 DDL |