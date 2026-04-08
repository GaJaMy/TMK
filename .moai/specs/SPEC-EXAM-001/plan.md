---
id: SPEC-EXAM-001
type: plan
version: "1.0.0"
created: "2026-04-07"
updated: "2026-04-07"
methodology: DDD (ANALYZE-PRESERVE-IMPROVE)
---

# SPEC-EXAM-001: Implementation Plan (구현 계획)

## 1. Methodology

**DDD (ANALYZE-PRESERVE-IMPROVE)** 방법론을 적용한다.

- **ANALYZE**: 기존 Exam 도메인 코드 구조와 동작 분석
- **PRESERVE**: 기존 동작 보존을 위한 특성화 테스트 작성
- **IMPROVE**: Stub 서비스를 실제 로직으로 구현

## 2. Implementation Milestones

### Primary Goal: Core Service 구현 (6개 서비스)

기존 Stub 서비스들을 실제 비즈니스 로직으로 구현한다.

**Task 1: GetExamService 구현**
- 파일: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamService.java`
- 내용: ExamPort.findByIdAndUserId() 호출, 미존재 시 EXAM_001 예외
- 참조: LoginService 패턴 (Port 주입, BusinessException, 엔티티 반환)
- 의존: ExamPort (기존)

**Task 2: SaveAnswerService 구현**
- 파일: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/SaveAnswerService.java`
- 내용: 상태 검증(IN_PROGRESS) + 만료 검증 + Exam.saveAnswer() + 저장
- 의존: GetExamService, ExamPort

**Task 3: SubmitExamService 구현**
- 파일: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/SubmitExamService.java`
- 내용: 시험 로드 -> 상태/만료 검증 -> ExamGradingService.grade() -> Exam.submit() -> 저장
- 의존: GetExamService, ExamGradingService, ExamPort

**Task 4: GetExamResultService 구현**
- 파일: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamResultService.java`
- 내용: 시험 조회 + 소유권 검증 + SUBMITTED/EXPIRED 상태 확인
- 의존: GetExamService

**Task 5: GetExamHistoryService 구현**
- 파일: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamHistoryService.java`
- 내용: 사용자 ID 기준 시험 이력 페이징 조회
- 의존: ExamPort
- 참고: ExamPort에 페이징 메서드 시그니처 확인 필요

**Task 6: GetExamHistoryDetailService 구현**
- 파일: `tmk-core/src/main/java/com/tmk/core/domain/exam/service/GetExamHistoryDetailService.java`
- 내용: 시험 상세 이력 (문제별 답안 + 채점 결과) 조회
- 의존: GetExamService

### Secondary Goal: ExamUseCase 구현 (DTO 변환 계층)

ExamUseCase의 7개 메서드에서 Entity -> DTO 변환 로직을 구현한다.

**Task 7: ExamUseCase 구현**
- 파일: `tmk-api/src/main/java/com/tmk/api/adapter/in/exam/usecase/ExamUseCase.java`
- 내용: 7개 메서드의 서비스 호출 + Entity -> DTO 매핑
- 참조: QuestionUseCase 패턴 (서비스 호출 -> 스트림 매핑 -> DTO 생성)
- 의존: 6개 Core Service + CreateExamService

### Final Goal: 단위 테스트 작성

각 서비스별 단위 테스트를 작성하여 비즈니스 로직을 검증한다.

**Task 8: GetExamService 테스트**
- 파일: `tmk-core/src/test/java/com/tmk/core/domain/exam/service/GetExamServiceTest.java`
- 시나리오: 정상 조회, 미존재 시험, 다른 사용자 시험
- 참조: VerifyEmailServiceTest 패턴 (@ExtendWith, @Mock, AssertJ)

**Task 9: SaveAnswerService 테스트**
- 파일: `tmk-core/src/test/java/com/tmk/core/domain/exam/service/SaveAnswerServiceTest.java`
- 시나리오: 정상 저장, SUBMITTED 상태, EXPIRED 상태, 만료된 시험

**Task 10: SubmitExamService 테스트**
- 파일: `tmk-core/src/test/java/com/tmk/core/domain/exam/service/SubmitExamServiceTest.java`
- 시나리오: 정상 제출 + 채점, 이미 제출, 만료된 시험

**Task 11: GetExamResultService 테스트**
- 파일: `tmk-core/src/test/java/com/tmk/core/domain/exam/service/GetExamResultServiceTest.java`
- 시나리오: 정상 결과 조회, 미제출 시험, 미존재 시험

**Task 12: GetExamHistoryService 테스트**
- 파일: `tmk-core/src/test/java/com/tmk/core/domain/exam/service/GetExamHistoryServiceTest.java`
- 시나리오: 이력 조회, 빈 이력, 페이징

**Task 13: GetExamHistoryDetailService 테스트**
- 파일: `tmk-core/src/test/java/com/tmk/core/domain/exam/service/GetExamHistoryDetailServiceTest.java`
- 시나리오: 상세 이력 조회, 미제출 시험

**Task 14: ExamUseCase 테스트**
- 파일: `tmk-api/src/test/java/com/tmk/api/adapter/in/exam/usecase/ExamUseCaseTest.java`
- 시나리오: 각 메서드의 DTO 변환 정확성 검증

### Optional Goal: 위험 완화

- ExamPort 페이징 메서드 추가 (필요 시)
- AnswerCommand null 체크 로직 추가

## 3. Technical Approach

### DDD ANALYZE Phase

1. 기존 Exam, ExamQuestion 엔티티의 비즈니스 메서드 동작 분석
2. ExamCreationService, ExamGradingService의 로직 흐름 확인
3. ExamPort, QuestionPort 인터페이스 시그니처 확인 (특히 페이징 관련)
4. 기존 CreateExamService 구현 패턴을 참조 패턴으로 확정
5. ExamController -> ExamUseCase -> Service 호출 체인 확인

### DDD PRESERVE Phase

1. 기존 CreateExamService 동작 보존 확인 (Stub이 아닌 실제 구현이므로)
2. ExamCreationService, ExamGradingService에 대한 특성화 테스트 작성 검토
3. 기존 엔티티 메서드 (submit, saveAnswer, isExpired, grade)의 동작 특성화

### DDD IMPROVE Phase

1. GetExamService 구현 (가장 기본, 다른 서비스들이 의존)
2. SaveAnswerService, SubmitExamService 구현 (핵심 비즈니스 로직)
3. GetExamResultService, GetExamHistoryService, GetExamHistoryDetailService 구현 (조회 로직)
4. ExamUseCase DTO 변환 구현 (모든 서비스 완료 후)
5. 단위 테스트 작성

### Reference Implementation Patterns

| 패턴 | 참조 | 적용 대상 |
|------|------|-----------|
| Service 구현 | LoginService | GetExamService, SaveAnswerService, SubmitExamService 등 |
| UseCase DTO 변환 | QuestionUseCase | ExamUseCase |
| 단위 테스트 | VerifyEmailServiceTest | 모든 서비스 테스트 |

### Implementation Order (의존성 기반)

```
1. GetExamService (기본 조회, 다른 서비스들이 내부적으로 사용)
   |
   ├── 2. SaveAnswerService (GetExamService 의존)
   ├── 3. SubmitExamService (GetExamService + ExamGradingService 의존)
   ├── 4. GetExamResultService (GetExamService 의존)
   └── 5. GetExamHistoryDetailService (GetExamService 의존)
   
6. GetExamHistoryService (ExamPort만 의존, 독립적)

7. ExamUseCase (모든 서비스 의존)

8~14. 단위 테스트 (각 서비스별)
```

## 4. Risk Mitigation

| 위험 | 완화 전략 |
|------|-----------|
| 동시성 문제 | 현재 범위에서는 단일 사용자 시나리오로 한정. 테스트에 동시성 시나리오 미포함. |
| ExamPort 페이징 미지원 | ANALYZE 단계에서 ExamPort 시그니처 확인. 미존재 시 메서드 추가. |
| DTO 매핑 복잡성 | QuestionUseCase 패턴을 엄격히 따름. 스트림 + 빌더 패턴 활용. |
| 기존 Stub 코드와의 충돌 | 기존 TODO stub을 완전 교체 (덮어쓰기). |

## 5. Files to Modify

### New Implementation (Stub -> Real)

| 파일 | 모듈 | 작업 |
|------|------|------|
| GetExamService.java | tmk-core | Stub -> 실제 구현 |
| SaveAnswerService.java | tmk-core | Stub -> 실제 구현 |
| SubmitExamService.java | tmk-core | Stub -> 실제 구현 |
| GetExamResultService.java | tmk-core | Stub -> 실제 구현 |
| GetExamHistoryService.java | tmk-core | Stub -> 실제 구현 |
| GetExamHistoryDetailService.java | tmk-core | Stub -> 실제 구현 |
| ExamUseCase.java | tmk-api | 7개 메서드 DTO 변환 구현 |

### New Test Files

| 파일 | 모듈 |
|------|------|
| GetExamServiceTest.java | tmk-core |
| SaveAnswerServiceTest.java | tmk-core |
| SubmitExamServiceTest.java | tmk-core |
| GetExamResultServiceTest.java | tmk-core |
| GetExamHistoryServiceTest.java | tmk-core |
| GetExamHistoryDetailServiceTest.java | tmk-core |
| ExamUseCaseTest.java | tmk-api |

### Potentially Modified (ANALYZE 단계에서 확인)

| 파일 | 모듈 | 조건 |
|------|------|------|
| ExamPort.java | tmk-core | 페이징 메서드 미존재 시 추가 |
| ExamRepository.java | tmk-api | ExamPort 변경 시 구현체 업데이트 |
