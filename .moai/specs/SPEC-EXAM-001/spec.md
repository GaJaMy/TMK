---
id: SPEC-EXAM-001
version: "1.0.0"
status: implemented
created: "2026-04-07"
updated: "2026-04-08"
author: GaJaMy
priority: high
issue_number: 0
lifecycle: spec-first
related_specs: [SPEC-DOC-001, SPEC-AUTH-002]
---

# SPEC-EXAM-001: Exam Feature Implementation (시험 기능 구현)

## HISTORY

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|-----------|
| 1.0.0 | 2026-04-07 | GaJaMy | 최초 작성 |

## 1. Environment

- **Project**: TMK (Test My Knowledge) - AI 기반 문제은행 플랫폼
- **Module**: tmk-core (순수 도메인), tmk-api (Spring Security, JWT, JPA)
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.5.11
- **Database**: PostgreSQL + pgvector 확장
- **Cache**: Redis (시험 임시 답안 저장, JWT refresh token)
- **Architecture**: Clean Architecture (Hexagonal) - Port & Adapter 패턴
- **Dependency Direction**: tmk-api -> tmk-core <- tmk-batch (core는 외부 의존 없음)
- **Batch**: tmk-batch (ExamAutoSubmitJob - 매 1분 만료 시험 자동 제출)
- **Development Mode**: DDD (ANALYZE-PRESERVE-IMPROVE)

## 2. Assumptions

- **AS-1**: Exam, ExamQuestion 엔티티가 tmk-core에 이미 존재하며, `submit()`, `saveAnswer()`, `isExpired()`, `grade()` 비즈니스 메서드가 완전히 구현되어 있다.
- **AS-2**: ExamCreationService, CreateExamService, ExamGradingService가 이미 구현 완료 상태이다.
- **AS-3**: ExamPort에 `findById()`, `findByIdAndUserId()`, `save()`, `findByUserIdOrderByCreatedAtDesc()` 메서드가 정의되어 있다.
- **AS-4**: QuestionPort에 `findGroupedByDifficulty()`, `findAllByIds()` 메서드가 정의되어 있다.
- **AS-5**: ExamController에 7개 엔드포인트가 모두 정의되어 있으며, ExamUseCase를 호출하는 구조이다.
- **AS-6**: ExamUseCase 인터페이스에 7개 메서드가 정의되어 있으나, 구현체는 TODO stub 상태이다.
- **AS-7**: 모든 DTO (ExamResult, ExamDetailResult, ExamQuestionResult, AnswerCommand, SubmitResult, ExamResultData, HistoryListResult, HistorySummary, HistoryDetailResult, HistoryQuestionResult)가 이미 정의되어 있다.
- **AS-8**: ErrorCode에 EXAM_001(시험 미존재), EXAM_002(이미 제출됨), EXAM_003(시험 만료), EXAM_004(문제 부족), QUESTION_002(문제 미존재)가 정의되어 있다.
- **AS-9**: BusinessException(ErrorCode) 도메인 예외 패턴이 이미 사용 중이다.
- **AS-10**: SecurityConfig에 `/api/**` 경로가 인증 필수로 설정되어 있다.
- **AS-11**: ExamAutoSubmitJob (tmk-batch)이 매 1분마다 만료된 시험을 자동 제출 및 채점 처리한다.

## 3. Requirements

### 3.1 Event-Driven Requirements (WHEN ... THEN ...)

- **REQ-EXAM-001**: WHEN 인증된 사용자가 시험 생성을 요청하면, THEN 시스템은 ExamCreationService를 통해 난이도별 최소 1문제를 포함한 최소 10문제를 선택하고, Exam 엔티티를 IN_PROGRESS 상태로 생성하여 ExamResult DTO로 반환해야 한다.
  - **구현 범위**: CreateExamService는 이미 완료. ExamUseCase에서 Entity -> ExamResult DTO 변환 로직만 구현 필요.

- **REQ-EXAM-002**: WHEN 인증된 사용자가 자신의 시험을 조회 요청하면, THEN 시스템은 시험 ID와 사용자 ID로 소유권을 검증한 후, 시험 정보와 문제 목록을 ExamDetailResult DTO로 반환해야 한다.
  - **구현 범위**: GetExamService 신규 구현 + ExamUseCase DTO 변환.

- **REQ-EXAM-003**: WHEN 인증된 사용자가 진행 중인 시험에 답안을 저장하면, THEN 시스템은 시험 상태가 IN_PROGRESS이고 만료되지 않았음을 검증한 후, 해당 문제의 답안을 저장해야 한다.
  - **구현 범위**: SaveAnswerService 신규 구현 + ExamUseCase 호출.

- **REQ-EXAM-004**: WHEN 인증된 사용자가 진행 중인 시험을 제출하면, THEN 시스템은 시험을 로드하고, ExamGradingService로 채점한 후, Exam.submit()으로 상태를 SUBMITTED로 변경하고, 채점 결과(정답 수, 총 문제 수, 합격 여부)를 SubmitResult DTO로 반환해야 한다.
  - **구현 범위**: SubmitExamService 신규 구현 + ExamUseCase DTO 변환.

- **REQ-EXAM-005**: WHEN 인증된 사용자가 제출 완료된 시험의 결과를 조회하면, THEN 시스템은 소유권 검증 후 시험 결과(점수, 합격 여부, 문제별 정답/오답)를 ExamResultData DTO로 반환해야 한다.
  - **구현 범위**: GetExamResultService 신규 구현 + ExamUseCase DTO 변환.

- **REQ-EXAM-006**: WHEN 인증된 사용자가 시험 이력 목록을 요청하면, THEN 시스템은 해당 사용자의 시험 이력을 최신순으로 페이징하여 HistoryListResult DTO로 반환해야 한다.
  - **구현 범위**: GetExamHistoryService 신규 구현 + ExamUseCase DTO 변환.

- **REQ-EXAM-007**: WHEN 인증된 사용자가 특정 시험의 상세 이력을 요청하면, THEN 시스템은 소유권 검증 후 해당 시험의 문제별 사용자 답안과 정답, 채점 결과를 HistoryDetailResult DTO로 반환해야 한다.
  - **구현 범위**: GetExamHistoryDetailService 신규 구현 + ExamUseCase DTO 변환.

### 3.2 State-Driven Requirements (IF ... THEN ...)

- **REQ-EXAM-008**: IF 시험 상태가 SUBMITTED 또는 EXPIRED이면, THEN 시스템은 답안 저장 요청 시 `EXAM_002` (이미 제출됨) 예외를 발생시켜야 한다.

- **REQ-EXAM-009**: IF 시험이 만료 시간(기본 30분)을 초과했으면, THEN 시스템은 답안 저장 및 제출 요청 시 `EXAM_003` (시험 만료) 예외를 발생시켜야 한다.

- **REQ-EXAM-010**: IF 요청한 시험이 존재하지 않거나 다른 사용자의 시험이면, THEN 시스템은 `EXAM_001` (시험 미존재) 예외를 발생시켜야 한다.

- **REQ-EXAM-011**: IF 시험 결과 조회 시 시험 상태가 IN_PROGRESS이면, THEN 시스템은 아직 제출되지 않은 시험으로 판단하여 적절한 예외를 발생시켜야 한다.

### 3.3 Ubiquitous Requirements (시스템은 항상 ... 해야 한다)

- **REQ-EXAM-012**: 시스템은 항상 시험 조회, 답안 저장, 제출, 결과 조회, 이력 조회 시 사용자 소유권 검증을 수행해야 한다 (`findByIdAndUserId` 사용).

- **REQ-EXAM-013**: 시스템은 항상 tmk-core 서비스에서 도메인 로직만 처리하고, DTO 변환은 tmk-api의 ExamUseCase에서 수행해야 한다 (Clean Architecture 의존성 방향 준수).

- **REQ-EXAM-014**: 시스템은 항상 합격 기준을 정답률 50% 이상으로 적용해야 한다.

### 3.4 Unwanted Requirements (시스템은 ... 하지 않아야 한다)

- **REQ-EXAM-015**: tmk-core 모듈의 서비스 클래스들은 Spring Framework, JPA, Redis 등 외부 프레임워크에 의존하지 않아야 한다 (Port 인터페이스를 통해서만 외부와 통신).

- **REQ-EXAM-016**: 시스템은 SUBMITTED 또는 EXPIRED 상태의 시험에 대해 답안 변경이나 재제출을 허용하지 않아야 한다.

## 4. Specifications

### 4.1 GetExamService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamService.java`

**역할**: 시험 ID와 사용자 ID로 시험 조회, 소유권 검증

**메서드**:
- `getExam(Long examId, Long userId)`: ExamPort.findByIdAndUserId() 호출, 미존재 시 BusinessException(EXAM_001) 발생, Exam 엔티티 반환

**패턴 참조**: LoginService (Port 주입, BusinessException 사용, 엔티티 반환)

### 4.2 SaveAnswerService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/SaveAnswerService.java`

**역할**: 진행 중인 시험에 답안 저장

**메서드**:
- `saveAnswer(Long examId, Long userId, Long questionId, String answer)`: 
  1. GetExamService로 시험 조회 + 소유권 검증
  2. 상태가 IN_PROGRESS가 아니면 BusinessException(EXAM_002)
  3. 만료 여부 확인 (isExpired()), 만료 시 BusinessException(EXAM_003)
  4. Exam.saveAnswer(questionId, answer) 호출
  5. ExamPort.save() 호출

### 4.3 SubmitExamService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/SubmitExamService.java`

**역할**: 시험 제출 오케스트레이션 (채점 + 상태 변경)

**메서드**:
- `submit(Long examId, Long userId)`:
  1. GetExamService로 시험 조회 + 소유권 검증
  2. 상태가 IN_PROGRESS가 아니면 BusinessException(EXAM_002)
  3. 만료 여부 확인, 만료 시 BusinessException(EXAM_003)
  4. ExamGradingService.grade(exam) 호출 (채점)
  5. Exam.submit() 호출 (상태 SUBMITTED로 변경)
  6. ExamPort.save() 호출
  7. Exam 엔티티 반환

### 4.4 GetExamResultService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamResultService.java`

**역할**: 제출 완료된 시험 결과 조회

**메서드**:
- `getResult(Long examId, Long userId)`:
  1. GetExamService로 시험 조회 + 소유권 검증
  2. 상태가 SUBMITTED 또는 EXPIRED가 아니면 예외 발생 (아직 미제출)
  3. Exam 엔티티 반환

### 4.5 GetExamHistoryService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamHistoryService.java`

**역할**: 사용자의 시험 이력 페이징 조회

**메서드**:
- `getHistory(Long userId, int page, int size)`:
  1. ExamPort.findByUserIdOrderByCreatedAtDesc(userId, page, size) 호출
  2. 결과 반환 (페이징된 Exam 리스트)

**참고**: ExamPort에 페이징 지원 메서드가 필요할 수 있음. 기존 메서드 시그니처 확인 후 필요 시 추가.

### 4.6 GetExamHistoryDetailService (tmk-core)

**위치**: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamHistoryDetailService.java`

**역할**: 특정 시험의 상세 이력 (문제별 답안 + 채점 결과)

**메서드**:
- `getDetail(Long examId, Long userId)`:
  1. GetExamService로 시험 조회 + 소유권 검증
  2. 상태가 SUBMITTED 또는 EXPIRED인지 검증
  3. Exam 엔티티 반환 (ExamQuestion 목록 포함)

### 4.7 ExamUseCase (tmk-api)

**위치**: `tmk-api/src/main/java/com/tmk/api/adapter/in/exam/usecase/ExamUseCase.java`

**역할**: Controller와 Core Service 사이의 매개체. Entity -> DTO 변환 담당.

**메서드 (7개)**:
1. `createExam(Long userId)` -> ExamResult
2. `getExam(Long examId, Long userId)` -> ExamDetailResult
3. `saveAnswer(Long examId, Long userId, AnswerCommand command)` -> void
4. `submitExam(Long examId, Long userId)` -> SubmitResult
5. `getExamResult(Long examId, Long userId)` -> ExamResultData
6. `getExamHistory(Long userId, int page, int size)` -> HistoryListResult
7. `getExamHistoryDetail(Long examId, Long userId)` -> HistoryDetailResult

**패턴 참조**: QuestionUseCase (서비스 호출 -> 스트림 매핑 -> DTO 생성)

## 5. Risks and Mitigation

| 위험 | 영향도 | 완화 전략 |
|------|--------|-----------|
| 시험 제출 동시성 문제 (race condition) | 높음 | 현재 SPEC 범위에서는 단일 사용자 시나리오로 한정. 추후 SPEC에서 낙관적 락 또는 Redis 분산 락 도입 검토. |
| QuestionPort.findGroupedByDifficulty() 메모리 이슈 | 중간 | 기존 구현 유지 (CreateExamService 범위). 문제 수 증가 시 별도 SPEC으로 최적화. |
| AnswerCommand에 @Valid 미적용 | 낮음 | ExamUseCase에서 null 체크 수행. 추후 Controller 레벨 검증 추가 권장. |
| ExamQuestion.questionId FK 부재 | 낮음 | 삭제된 문제 참조 시 null 처리. 추후 DB 마이그레이션 SPEC에서 FK 추가 검토. |

## 6. Traceability

| Requirement | Service | UseCase Method | Endpoint | Test |
|-------------|---------|---------------|----------|------|
| REQ-EXAM-001 | CreateExamService (기존) | createExam() | POST /exams | TBD |
| REQ-EXAM-002 | GetExamService | getExam() | GET /exams/{id} | TBD |
| REQ-EXAM-003 | SaveAnswerService | saveAnswer() | PUT /exams/{id}/answers | TBD |
| REQ-EXAM-004 | SubmitExamService | submitExam() | POST /exams/{id}/submit | TBD |
| REQ-EXAM-005 | GetExamResultService | getExamResult() | GET /exams/{id}/result | TBD |
| REQ-EXAM-006 | GetExamHistoryService | getExamHistory() | GET /exams/history | TBD |
| REQ-EXAM-007 | GetExamHistoryDetailService | getExamHistoryDetail() | GET /exams/history/{id} | TBD |
