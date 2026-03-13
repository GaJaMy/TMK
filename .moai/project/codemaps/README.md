# TMK 아키텍처 코드맵

TMK (Test My Knowledge) 프로젝트의 완전한 아키텍처 문서입니다. 이 코드맵은 Java/Spring Boot 멀티 모듈 프로젝트를 이해하는 데 필요한 모든 정보를 포함합니다.

## 📚 문서 구조

### 1. [overview.md](./overview.md) - 아키텍처 개요
**읽는 시간**: 15분 | **난이도**: 초급

프로젝트 전체 구조를 파악하는 출발점입니다.

**주요 내용**:
- 프로젝트 소개 및 기술 스택
- 계층별 아키텍처 (REST API, Adapter, UseCase, Domain, Port, Infrastructure)
- 모듈 책임 (tmk-core, tmk-api, tmk-batch)
- 클린 아키텍처 원칙과 의존성 방향
- 도메인 모델 규칙 (User, Document, Question, Exam)
- 시스템 컨텍스트 다이어그램
- 주요 설계 결정 사항
- API 규칙 및 데이터베이스 설계

**누가 읽을까?**
- 새로운 팀원이 프로젝트를 이해하려 할 때
- 아키텍처 전체상을 파악하려 할 때
- 주요 설계 결정의 이유를 알고 싶을 때

---

### 2. [modules.md](./modules.md) - 모듈 상세 구조
**읽는 시간**: 25분 | **난이도**: 중급

각 모듈의 내부 구조와 책임을 깊이 있게 설명합니다.

**주요 내용**:
- **tmk-core** (순수 도메인 모듈)
  - 패키지 구조 (domain/, port/)
  - 주요 도메인 엔티티 (User, EmailVerification, Document, DocumentChunk, Question, Exam)
  - UseCase 인터페이스 패턴
  - Repository 인터페이스
  - Domain Service (ExamCreationService, ExamGradingService)

- **tmk-api** (REST API 및 어댑터 모듈)
  - 패키지 구조 (controller/, config/, security/, adapter/, common/)
  - Controller 상세 (Auth, Document, Question, Exam, Internal)
  - Adapter 패턴 (Incoming, Outgoing)
  - 인증 흐름 (JWT, Spring Security)
  - Adapter-Port 변환

- **tmk-batch** (배치 처리 모듈)
  - 패키지 구조 (job/, config/, listener/, service/)
  - ExamAutoSubmitJob (1분 주기)
  - ExpiredVerificationCleanJob (매일 새벽)
  - Spring Batch 구성 (ItemReader, ItemProcessor, ItemWriter)
  - 스케줄 설정

- 모듈 간 통신 흐름
- 의존성 규칙

**누가 읽을까?**
- 특정 모듈의 구현을 이해하려 할 때
- 새로운 기능을 추가할 때 정확한 위치를 찾을 때
- 클래스 구조와 책임을 파악하려 할 때

---

### 3. [dependencies.md](./dependencies.md) - 외부 의존성 관리
**읽는 시간**: 15분 | **난이도**: 초급-중급

프로젝트의 모든 외부 라이브러리와 버전을 관리합니다.

**주요 내용**:
- 모듈 간 의존성 그래프 (tmk-api, tmk-batch → tmk-core)
- 상세 의존성 구조 및 버전
- 외부 라이브러리 목록
  - Spring Boot Starters (web, security, data-jpa, data-redis, batch)
  - 데이터베이스 (PostgreSQL, pgvector)
  - AI/ML 통합 (Spring AI, OpenAI)
  - 보안 (JWT - jjwt)
  - 데이터 처리 (PDFBox, Jackson)
  - 로깅 (Logback, Micrometer)
  - 테스트 (JUnit, Mockito, TestContainers)
- 아키텍처별 의존성
- 버전 관리 전략
- 의존성 최소화 원칙
- 의존성 충돌 해결
- 보안 업데이트 전략

**누가 읽을까?**
- 라이브러리 버전을 업그레이드할 때
- 새로운 라이브러리를 추가할 때
- 보안 취약점을 확인할 때
- 빌드 관련 문제를 해결할 때

---

### 4. [entry-points.md](./entry-points.md) - 애플리케이션 진입점 및 엔드포인트
**읽는 시간**: 30분 | **난이도**: 중급

모든 REST API 엔드포인트와 배치 작업을 상세히 설명합니다.

**주요 내용**:
- **애플리케이션 진입점**
  - TmkApiApplication (REST API 서버)
  - TmkBatchApplication (배치 처리 서버)

- **REST API 엔드포인트** (전체 명세)
  - 인증 API (AuthController)
    - 이메일 인증코드 발송
    - 이메일 인증
    - 회원가입
    - 로그인
    - 토큰 재발급
    - 소셜 로그인 (Google, Naver, Kakao)
    - 로그아웃

  - 문서 API (DocumentController)
    - 문서 목록 조회
    - 문서 상세 조회
    - 문서 처리 상태 조회
    - 내부 API: 문서 등록

  - 질문 API (QuestionController)
    - 질문 목록 조회
    - 질문 상세 조회 (선택지 포함)

  - 시험 API (ExamController)
    - 시험 생성
    - 진행 중인 시험 조회
    - 답안 저장 (임시)
    - 시험 제출 및 채점
    - 시험 결과 조회
    - 시험 히스토리 (목록, 상세)

- **Spring Batch 작업**
  - ExamAutoSubmitJob (매 1분 실행)
  - ExpiredVerificationCleanJob (매일 02:00 AM 실행)

- **권한 검증** (Spring Security)
- **에러 처리** 및 응답 코드
- **성능 고려사항** (페이지네이션, 캐싱, 연결 풀)

**누가 읽을까?**
- API 엔드포인트 명세를 찾을 때
- 새로운 API를 설계할 때
- 테스트 시나리오를 작성할 때
- 배치 작업 흐름을 이해할 때

---

### 5. [data-flow.md](./data-flow.md) - 데이터 처리 흐름
**읽는 시간**: 35분 | **난이도**: 고급

복잡한 비즈니스 프로세스의 데이터 흐름을 시각화합니다.

**주요 내용**:
- **문서 처리 파이프라인** (PDF → 질문 생성)
  1. PDF 텍스트 추출 (Apache PDFBox)
  2. 청킹 (512 토큰, 128 오버랩)
  3. 임베딩 생성 (OpenAI text-embedding-3-small, 1536차원)
  4. pgvector에 저장 (HNSW 인덱스)
  5. 질문 생성 (OpenAI GPT)
  6. 질문 저장

- **시험 생성 및 진행 흐름**
  - 시험 생성 (난이도 분포 고려)
  - 시험 진행 중 답안 저장 (Redis)
  - 시험 제출 및 채점
  - 결과 저장 및 조회

- **인증 흐름**
  - 로그인 시퀀스 (이메일/비밀번호 검증)
  - JWT 토큰 생성 (Access Token, Refresh Token)
  - 토큰 검증 (protected 요청)
  - JWT 토큰 구조

- **Redis 캐시 전략**
  - 캐시 키 설계
  - 캐시 무효화 이벤트

- **배치 작업 데이터 흐름**
  - ExamAutoSubmitJob
  - ExpiredVerificationCleanJob

- **의존성 데이터 흐름** (Document → Question → Exam)
- **데이터 일관성 보장** (트랜잭션, 분산 일관성)
- **성능 최적화 포인트** (인덱스, 쿼리 최적화)

**누가 읽을까?**
- 복잡한 비즈니스 흐름을 이해하려 할 때
- 성능 병목을 분석할 때
- 데이터 일관성 문제를 디버깅할 때
- 새로운 기능을 추가할 때 데이터 흐름을 파악해야 할 때

---

## 🎯 읽기 가이드

### 상황별 추천 경로

**상황 1: 프로젝트에 새로 합류한 개발자**
```
1. overview.md 읽기 (15분)
   → 전체 구조 파악

2. modules.md 읽기 (25분)
   → 각 모듈의 책임 이해

3. entry-points.md 읽기 (30분)
   → 주요 API와 배치 작업 확인

4. 필요시 data-flow.md 참고
   → 복잡한 부분 상세 분석
```
**총 소요 시간: 1시간 20분**

---

**상황 2: 특정 기능을 구현할 때**
```
1. overview.md에서 도메인 모델 확인 (5분)

2. modules.md에서 해당 모듈 찾기 (10분)

3. entry-points.md에서 관련 API 확인 (10분)

4. data-flow.md에서 데이터 흐름 분석 (15분)

5. 코드 구현
```
**총 소요 시간: 40분 + 구현**

---

**상황 3: 성능 최적화**
```
1. overview.md에서 현재 아키텍처 이해 (5분)

2. data-flow.md의 성능 최적화 섹션 읽기 (10분)

3. dependencies.md에서 관련 라이브러리 확인 (5분)

4. 최적화 적용
```
**총 소요 시간: 20분 + 구현**

---

**상황 4: 버그 디버깅**
```
1. entry-points.md에서 관련 엔드포인트 확인 (10분)

2. data-flow.md에서 데이터 흐름 추적 (15분)

3. modules.md에서 구현 코드 위치 찾기 (5분)

4. 코드 분석 및 수정
```
**총 소요 시간: 30분 + 분석**

---

## 📊 핵심 개념 정리

### 클린 아키텍처
- **tmk-core**: 순수 도메인 (프레임워크 독립적)
- **tmk-api**: Spring Framework 적용 (REST, Security, JPA)
- **tmk-batch**: Spring Batch 적용 (스케줄된 작업)

### 의존성 방향
```
tmk-api → tmk-core ← tmk-batch
↓
PostgreSQL, Redis, OpenAI API
```

### 주요 도메인
| 도메인 | 책임 | 주요 규칙 |
|--------|------|----------|
| **User** | 사용자 계정 관리 | provider/password 필수 조건 |
| **EmailVerification** | 이메일 인증 | 5분 유효기간 |
| **Document** | PDF 문서 관리 | 즉시 처리 파이프라인 시작 |
| **Question** | 생성된 질문 관리 | 최소 2개 생성, MULTIPLE_CHOICE는 5개 선택지 |
| **Exam** | 사용자 시험 | 최소 10문제, 50% 이상 합격 |

### 주요 기술
- **Java 21** + **Spring Boot 3.5.11**: 주요 프레임워크
- **PostgreSQL + pgvector**: 벡터 임베딩 기반 검색
- **Redis**: 캐시 및 임시 데이터 저장
- **OpenAI API**: 임베딩 및 질문 생성
- **Spring Security + JWT**: 인증/권한 관리
- **Spring Batch**: 스케줄된 배치 작업
- **Gradle**: 멀티 모듈 빌드

---

## 🔧 마이그레이션 및 확장

### 새로운 모듈 추가 시
1. tmk-core에 Domain 엔티티 정의
2. tmk-core에 UseCase 인터페이스 정의
3. tmk-core에 Repository 인터페이스 정의
4. tmk-api에 UseCase 구현 및 Repository 구현 (JPA)
5. tmk-api에 Controller 추가
6. entry-points.md 업데이트

### 새로운 배치 작업 추가 시
1. tmk-batch에 Job 정의
2. JobSchedulingConfig에 스케줄 등록
3. JobExecutionListener 추가 (선택)
4. data-flow.md의 배치 섹션 업데이트

### 기술 스택 변경 시
- tmk-core는 변경 없음 (프레임워크 독립적)
- tmk-api만 변경 (예: JPA → MyBatis)
- dependencies.md 업데이트

---

## 📈 버전 정보

**작성 일자**: 2025-03-12
**대상 프로젝트**: TMK (Test My Knowledge) v1.0
**Java 버전**: 21 (LTS)
**Spring Boot 버전**: 3.5.11

---

## 🚀 다음 단계

### 개발자 입장에서
1. 각 문서를 읽고 필요시 역참조 ([modules.md](./modules.md) ← → [entry-points.md](./entry-points.md))
2. 실제 코드와 비교하며 읽기
3. 새로운 기능 추가 시 이 문서를 업데이트하기

### 아키텍처 개선 시
1. [overview.md](./overview.md)의 "주요 설계 결정"을 검토
2. 영향받는 모듈 파악 ([modules.md](./modules.md))
3. 데이터 흐름 재분석 ([data-flow.md](./data-flow.md))
4. 엔드포인트 변경 반영 ([entry-points.md](./entry-points.md))

---

## 💡 팁

- **빠른 검색**: Ctrl+F로 문서 내 검색
- **도메인별 검색**: "Exam", "Question", "Document" 등의 도메인 이름으로 검색
- **기술별 검색**: "PostgreSQL", "Redis", "OpenAI" 등으로 검색
- **엔드포인트 검색**: "POST /api/v1/exams" 등의 HTTP 메서드와 경로로 검색

---

**문서 작성**: MoAI Documentation System
**언어**: 한국어
**대상 읽자**: TMK 프로젝트 팀원
