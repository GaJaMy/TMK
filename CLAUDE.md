# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**TMK (Test My Knowledge)** — AI 기반 문제은행 플랫폼. PDF 문서를 등록하면 OpenAI를 통해 자동으로 문제를 생성하고, 사용자가 시험을 응시하여 학습 이해도를 확인할 수 있는 서비스.

## Commands

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트 전체
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.tmk.tmk.SomeTest"

# 빌드 없이 테스트만
./gradlew test --rerun-tasks
```

- Java 21, Spring Boot 3.5.x, Gradle
- DB: PostgreSQL + pgvector 확장 (`CREATE EXTENSION IF NOT EXISTS vector;` 필요)
- Redis: 이메일 인증 코드, JWT refresh token, 시험 진행 중 임시 답안 저장

## Architecture

### 멀티 모듈 구조 (클린 아키텍처)

```
tmk-parent/
├── tmk-core/                  # 순수 도메인. Spring/JPA 등 외부 프레임워크 미의존
│   ├── domain/                # Aggregate, Entity, Value Object, Enum
│   │   ├── user/
│   │   ├── emailverification/
│   │   ├── document/
│   │   ├── question/
│   │   └── exam/              # ExamCreationService, ExamGradingService 포함
│   └── port/
│       ├── in/                # UseCase 인터페이스 (도메인별 패키지)
│       │   ├── auth/          # SendEmailVerification, Verify, Register, Login, Logout, Social, Reissue
│       │   │   └── dto/       # LoginResult, ReissueResult, SocialLoginResult
│       │   ├── document/      # RegisterDocument, GetDocumentStatus
│       │   │   └── dto/       # RegisterDocumentResult, DocumentStatusResult
│       │   ├── question/      # GetQuestionList, GetQuestionDetail
│       │   │   └── dto/       # OptionResult, QuestionDetailResult, QuestionSummary, QuestionListResult
│       │   └── exam/          # CreateExam, GetExam, SaveAnswer, SubmitExam, GetExamResult, GetExamHistory(Detail)
│       │       └── dto/       # ExamResult, ExamDetailResult, AnswerCommand, SubmitResult, History...
│       └── out/               # Repository 인터페이스
│           ├── UserRepository
│           ├── EmailVerificationRepository
│           ├── DocumentRepository / DocumentChunkRepository
│           ├── QuestionRepository
│           └── ExamRepository
│
├── tmk-api/                   # REST API, Spring Security, JWT, JPA 구현체
│   ├── controller/
│   ├── common/                # ApiResponse
│   ├── security/
│   └── adapter/
│       ├── in/                # Controller → UseCase 호출
│       └── out/               # Repository 구현체 (JPA Adapter)
│
└── tmk-batch/                 # Spring Batch
    └── job/
        ├── ExamAutoSubmitJob        # 매 1분: 만료 시험 자동 제출 및 채점
        └── ExpiredVerificationCleanJob  # 매일 새벽: 만료 인증코드 삭제
```

**의존성 방향**: `tmk-api` → `tmk-core` ← `tmk-batch` (core는 외부에 의존하지 않음)

### 도메인 목록

| 도메인 | Aggregate Root | 주요 규칙 |
|--------|---------------|-----------|
| User | User | provider=LOCAL이면 password 필수; 소셜이면 providerId 필수 |
| EmailVerification | EmailVerification | 코드 5분 유효; 인증 완료 후에만 회원가입 가능 |
| Document | Document | 등록 즉시 파이프라인 실행; 하나의 문서에서 최소 2개 문제 생성 |
| Question | Question | MULTIPLE_CHOICE는 options 5개 필수; 나머지는 options 없음 |
| Exam | Exam | 최소 10문제; 난이도별 최소 1문제; 정답률 50% 이상 합격 |

### 문서 처리 파이프라인

```
[내부 API] PDF 수신 → 텍스트 파싱 → 청킹 → OpenAI 임베딩(text-embedding-3-small, 1536차원)
→ pgvector 저장 → ANN 검색(코사인 유사도) → LLM 문제 생성 → QUESTION 저장
```

MVP에서는 내부 API(`/internal/v1/documents`)를 통해서만 문서를 등록하며, 사용자는 직접 등록 불가.

### API 규칙

- Base URL: `/api/v1` (공개), `/internal/v1` (내부 전용)
- 공통 응답: `{ "errorCode": "SUCCESS", "msg": "ok", "data": {} }`
- 인증: `Authorization: Bearer {accessToken}` (JWT)
- 에러코드 패턴: `AUTH_00x`, `EXAM_00x`, `QUESTION_00x`, `COMMON_00x`

### DB 특이사항

- `TIMESTAMPTZ` 컬럼 → JPA에서 `OffsetDateTime` 또는 `Instant` 매핑
- `BIGSERIAL` → `GenerationType.IDENTITY` 또는 `SEQUENCE`
- Enum 컬럼은 PostgreSQL 네이티브 ENUM 대신 `VARCHAR + CHECK` 사용 (JPA 호환성)
- `DOCUMENT_CHUNK.embedding`은 `vector(1536)` 타입 (pgvector)
- HNSW 인덱스: `m=16, ef_construction=64` (코사인 유사도 ANN)
- Redis에서 TTL 기반 관리: 이메일 인증코드, refresh token, 시험 임시 답안