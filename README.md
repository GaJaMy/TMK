# TMK (Test My Knowledge)

> AI 기반 문제은행 플랫폼 — PDF 문서를 등록하면 자동으로 문제를 생성하고, 시험을 통해 학습 이해도를 확인할 수 있는 서비스

---

## 주요 기능

- **AI 문제 자동 생성**: PDF 또는 MD 문서를 등록하면 OpenAI를 통해 객관식·단답형·참/거짓 문제를 자동 생성
- **RAG 기반 문제 생성**: 문서를 청킹·임베딩하여 pgvector에 저장하고, 문서 맥락에 기반한 개인 문제를 생성
- **시험 응시**: 공용 Topic 또는 개인 문서 기반으로 시험을 생성하고 직접 시작해 응시
- **자동 제출 및 채점**: 시험 시간 만료 시 batch가 자동 제출과 채점을 처리
- **결과 분석**: 시험 제출 후 점수, 정답 수, 문제별 정답·해설 확인 가능
- **사용자/관리자 분리 인증**: 일반 사용자와 관리자가 서로 다른 인증 경로와 JWT 비밀키를 사용
- **Redis 토큰 관리**: refresh token 저장과 access token blacklist 관리
- **Flyway 기반 스키마 관리**: 공통 DB 마이그레이션을 `tmk-core`의 `db/migration`에서 관리

---

## 개발 메모

- **AI 에이전트 활용 개발**: OpenAI Codex 기반 AI 에이전트를 활용해 API 설계 보조, 화면 연동, 문서 정리, 테스트 보강 작업을 함께 진행
- **데모용 프론트 포함**: `tmk-user-web`, `tmk-admin-web`에 사용자/관리자 흐름을 확인할 수 있는 데모용 웹 화면을 구현

---

## 기술 스택

채용 관점에서 이 프로젝트의 핵심 기술은 아래 조합입니다.

- `Java 21`
- `Spring Boot 3.5`
- `Spring Security + JWT`
- `PostgreSQL + pgvector`
- `Redis`
- `Spring Batch`
- `Flyway`
- `OpenAI API`
- `QueryDSL`
- `Apache PDFBox`

개발 도구 및 개발 방식:

- `Docker`
- `GitHub Actions`
- `OpenAI Codex`

| 분류 | 기술 | 포인트 |
|------|------|------|
| Language | Java 21 | 최신 LTS 기반 |
| Backend | Spring Boot 3.5 | 사용자/관리자 API, 비동기 문서 처리 |
| Security | Spring Security, JWT | 사용자/관리자 분리 인증, 토큰 재발급/블랙리스트 |
| Database | PostgreSQL, JPA | 사용자/문서/시험/관리자 도메인 저장 |
| Vector Search | pgvector | 문서 청킹 임베딩 저장, RAG 검색 기반 |
| Cache | Redis | refresh token 저장, access token blacklist |
| Batch | Spring Batch | 시험 시간 만료 자동 제출 및 채점 |
| Migration | Flyway | 공통 DB 마이그레이션 관리 |
| AI | OpenAI API | 문서 임베딩, 문제/정답/해설 생성 |
| Query | QueryDSL | 관리자 검색/목록 조건 조회 |
| Document Parsing | Apache PDFBox | PDF 텍스트 추출 |
| DevOps | Docker, GitHub Actions | 이미지 빌드, 운영 배포 자동화 |
| AI-Assisted Development | OpenAI Codex | API 구현 보조, 테스트 보강, 문서 정리 |
| Build | Gradle Multi-module | `tmk-core`, `tmk-infra`, `tmk-api`, `tmk-batch` 분리 |

---

## 아키텍처

클린 아키텍처 기반 멀티 모듈 구조로 설계되었습니다.

```text
tmk-parent/
├── tmk-core/      # 도메인 엔티티, 애플리케이션 서비스, outbound port
├── tmk-infra/     # JPA repository, persistence adapter
├── tmk-api/       # 사용자 API + 관리자 API, Spring Security, JWT, 비동기 문서 처리
├── tmk-batch/     # Spring Batch
│   └── job/
│       └── ExamAutoSubmitJob          # 초 단위: 만료 시험 자동 제출 및 채점
├── tmk-user-web/  # 사용자 웹
└── tmk-admin-web/ # 관리자 웹
```

**의존성 방향**: `tmk-api` → `tmk-core`, `tmk-infra` / `tmk-batch` → `tmk-core`, `tmk-infra`

### 클린 아키텍처 준수 현황

| 항목 | 상태 | 내용 |
|------|------|------|
| 의존성 방향 | ✅ | 역방향 의존 없음. 빌드 레벨에서 강제 |
| 도메인 순수성 | ✅ | core Entity는 Jakarta Persistence + Lombok만 사용 |
| 도메인 로직 위치 | ✅ | `Exam.submit()`, `ExamQuestion.grade()` 등 Entity 내부 |
| Repository 추상화 | ✅ | 인터페이스는 core, JPA 구현체는 infra에 분리 |
| 예외 처리 | ✅ | `BusinessException`, `ErrorCode`가 core에 위치 |
| core의 Spring 설정 | ⚠️ | `RedisConfig`, `JwtProvider` 등이 core에 위치 (api·batch 공유 목적의 실용적 선택) |

### 문서 처리 파이프라인

```
PDF 등록 → 텍스트 파싱 → 청킹 → OpenAI 임베딩(1536차원)
→ pgvector 저장 → ANN 검색(코사인 유사도) → LLM 문제 생성 → DB 저장
```

## API 개요

사용자 인증 Base URL: `/api/auth/v1`
사용자 기능 Base URL: `/`
관리자 Base URL: `/admin/v1`

| 도메인 | Method | URL | 설명 | 인증 |
|--------|--------|-----|------|------|
| Auth | POST | `/api/auth/v1/register` | 회원가입 | ❌ |
| Auth | POST | `/api/auth/v1/login` | 로그인 | ❌ |
| Auth | POST | `/api/auth/v1/reissue` | 토큰 재발급 | ❌ |
| Auth | POST | `/api/auth/v1/logout` | 로그아웃 | ✅ |
| Auth | POST | `/api/auth/v1/reset-password` | 비밀번호 재설정 | ❌ |
| Topic | GET | `/api/topics` | 공용 Topic 목록 조회 | ✅ |
| Document | POST | `/api/my/documents/upload` | 문서 업로드 등록 | ✅ |
| Document | GET | `/api/my/documents` | 내 문서 목록 조회 | ✅ |
| Document | GET | `/api/my/documents/{id}/status` | 내 문서 상태 조회 | ✅ |
| Exam | POST | `/api/exams` | 시험 생성 | ✅ |
| Exam | POST | `/api/exams/{id}/start` | 시험 시작 | ✅ |
| Exam | GET | `/api/exams` | 생성/진행중 시험 목록 조회 | ✅ |
| Exam | GET | `/api/exams/{id}` | 시험 문제 조회 | ✅ |
| Exam | PUT | `/api/exams/{id}/answers` | 답안 저장 | ✅ |
| Exam | POST | `/api/exams/{id}/submit` | 시험 제출 | ✅ |
| Exam | GET | `/api/exams/{id}/result` | 시험 결과 조회 | ✅ |
| Exam | GET | `/api/exams/history` | 시험 히스토리 목록 | ✅ |
| Admin Auth | POST | `/admin/auth/v1/login` | 관리자 로그인 | ❌ |
| Admin Auth | POST | `/admin/auth/v1/reissue` | 관리자 토큰 재발급 | ❌ |
| Admin Auth | POST | `/admin/auth/v1/logout` | 관리자 로그아웃 | ✅ |

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
| [`docs/문서 인덱스.md`](docs/%EB%AC%B8%EC%84%9C%20%EC%9D%B8%EB%8D%B1%EC%8A%A4.md) | 전체 문서 목록 인덱스 |

환경 변수, 로컬 실행, 배포 절차는 [`docs/기술 스택.md`](docs/%EA%B8%B0%EC%88%A0%20%EC%8A%A4%ED%83%9D.md), [`docs/배포 가이드.md`](docs/%EB%B0%B0%ED%8F%AC%20%EA%B0%80%EC%9D%B4%EB%93%9C.md)를 참고하세요.
