---
id: SPEC-EXAM-001
type: acceptance
version: "1.0.0"
created: "2026-04-07"
updated: "2026-04-07"
---

# SPEC-EXAM-001: Acceptance Criteria (인수 기준)

## 1. REQ-EXAM-001: 시험 생성

### Scenario 1.1: 정상 시험 생성
```gherkin
Given 인증된 사용자가 존재하고
  And 문제은행에 난이도별(EASY, MEDIUM, HARD) 최소 1문제 이상, 총 10문제 이상이 존재할 때
When 사용자가 POST /api/v1/exams 요청을 보내면
Then 시스템은 IN_PROGRESS 상태의 시험을 생성하고
  And ExamResult DTO를 반환하며
  And HTTP 200 응답을 반환한다
```

### Scenario 1.2: 문제 부족 시 시험 생성 실패
```gherkin
Given 인증된 사용자가 존재하고
  And 문제은행에 10문제 미만이 존재할 때
When 사용자가 POST /api/v1/exams 요청을 보내면
Then 시스템은 EXAM_004 에러를 반환한다
```

## 2. REQ-EXAM-002: 시험 조회

### Scenario 2.1: 정상 시험 조회
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자가 생성한 IN_PROGRESS 상태의 시험이 존재할 때
When 사용자가 GET /api/v1/exams/{examId} 요청을 보내면
Then 시스템은 시험 정보와 문제 목록을 포함한 ExamDetailResult DTO를 반환하고
  And HTTP 200 응답을 반환한다
```

### Scenario 2.2: 다른 사용자의 시험 조회 시도
```gherkin
Given 인증된 사용자 A가 존재하고
  And 사용자 B가 생성한 시험이 존재할 때
When 사용자 A가 GET /api/v1/exams/{examId} 요청을 보내면
Then 시스템은 EXAM_001 에러를 반환한다
```

### Scenario 2.3: 존재하지 않는 시험 조회
```gherkin
Given 인증된 사용자가 존재할 때
When 사용자가 존재하지 않는 examId로 GET /api/v1/exams/{examId} 요청을 보내면
Then 시스템은 EXAM_001 에러를 반환한다
```

## 3. REQ-EXAM-003: 답안 저장

### Scenario 3.1: 정상 답안 저장
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 IN_PROGRESS 상태 시험이 존재하고
  And 시험이 아직 만료되지 않았을 때
When 사용자가 PUT /api/v1/exams/{examId}/answers 요청에 questionId와 answer를 보내면
Then 시스템은 해당 문제의 답안을 저장하고
  And HTTP 200 응답을 반환한다
```

### Scenario 3.2: 이미 제출된 시험에 답안 저장 시도
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 SUBMITTED 상태 시험이 존재할 때
When 사용자가 PUT /api/v1/exams/{examId}/answers 요청을 보내면
Then 시스템은 EXAM_002 에러를 반환한다
```

### Scenario 3.3: 만료된 시험에 답안 저장 시도
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 IN_PROGRESS 상태 시험이 존재하지만
  And 시험 생성 시간으로부터 30분이 경과했을 때
When 사용자가 PUT /api/v1/exams/{examId}/answers 요청을 보내면
Then 시스템은 EXAM_003 에러를 반환한다
```

### Scenario 3.4: 다른 사용자의 시험에 답안 저장 시도
```gherkin
Given 인증된 사용자 A가 존재하고
  And 사용자 B의 시험이 존재할 때
When 사용자 A가 PUT /api/v1/exams/{examId}/answers 요청을 보내면
Then 시스템은 EXAM_001 에러를 반환한다
```

## 4. REQ-EXAM-004: 시험 제출 및 채점

### Scenario 4.1: 정상 시험 제출
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 IN_PROGRESS 상태 시험이 존재하고
  And 일부 문제에 답안이 저장되어 있을 때
When 사용자가 POST /api/v1/exams/{examId}/submit 요청을 보내면
Then 시스템은 ExamGradingService로 채점을 수행하고
  And 시험 상태를 SUBMITTED로 변경하고
  And SubmitResult DTO(정답 수, 총 문제 수, 합격 여부)를 반환하고
  And HTTP 200 응답을 반환한다
```

### Scenario 4.2: 50% 이상 정답 시 합격
```gherkin
Given 10문제 시험에서 5문제 이상 정답을 저장한 상태일 때
When 사용자가 시험을 제출하면
Then SubmitResult의 passed 필드가 true로 반환된다
```

### Scenario 4.3: 50% 미만 정답 시 불합격
```gherkin
Given 10문제 시험에서 4문제 이하 정답을 저장한 상태일 때
When 사용자가 시험을 제출하면
Then SubmitResult의 passed 필드가 false로 반환된다
```

### Scenario 4.4: 이미 제출된 시험 재제출 시도
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 SUBMITTED 상태 시험이 존재할 때
When 사용자가 POST /api/v1/exams/{examId}/submit 요청을 보내면
Then 시스템은 EXAM_002 에러를 반환한다
```

### Scenario 4.5: 만료된 시험 제출 시도
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 시험이 30분 경과하여 만료되었을 때
When 사용자가 POST /api/v1/exams/{examId}/submit 요청을 보내면
Then 시스템은 EXAM_003 에러를 반환한다
```

## 5. REQ-EXAM-005: 시험 결과 조회

### Scenario 5.1: 정상 결과 조회
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 SUBMITTED 상태 시험이 존재할 때
When 사용자가 GET /api/v1/exams/{examId}/result 요청을 보내면
Then 시스템은 시험 결과(점수, 합격 여부, 문제별 정답/오답)를 ExamResultData DTO로 반환하고
  And HTTP 200 응답을 반환한다
```

### Scenario 5.2: 미제출 시험 결과 조회 시도
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 IN_PROGRESS 상태 시험이 존재할 때
When 사용자가 GET /api/v1/exams/{examId}/result 요청을 보내면
Then 시스템은 적절한 예외를 반환한다
```

### Scenario 5.3: 다른 사용자의 시험 결과 조회 시도
```gherkin
Given 인증된 사용자 A가 존재하고
  And 사용자 B의 SUBMITTED 상태 시험이 존재할 때
When 사용자 A가 GET /api/v1/exams/{examId}/result 요청을 보내면
Then 시스템은 EXAM_001 에러를 반환한다
```

## 6. REQ-EXAM-006: 시험 이력 목록 조회

### Scenario 6.1: 정상 이력 조회
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자가 5개의 시험을 완료했을 때
When 사용자가 GET /api/v1/exams/history?page=0&size=10 요청을 보내면
Then 시스템은 최신순으로 정렬된 시험 이력을 HistoryListResult DTO로 반환하고
  And HTTP 200 응답을 반환한다
```

### Scenario 6.2: 이력이 없는 사용자
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자가 시험을 한 번도 치르지 않았을 때
When 사용자가 GET /api/v1/exams/history 요청을 보내면
Then 시스템은 빈 목록을 포함한 HistoryListResult DTO를 반환한다
```

### Scenario 6.3: 페이징 동작 검증
```gherkin
Given 인증된 사용자가 15개의 시험을 완료했을 때
When 사용자가 GET /api/v1/exams/history?page=0&size=10 요청을 보내면
Then 시스템은 10개의 시험 이력을 반환하고
  And 전체 개수 정보를 포함한다
```

## 7. REQ-EXAM-007: 시험 상세 이력 조회

### Scenario 7.1: 정상 상세 이력 조회
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 SUBMITTED 상태 시험이 존재할 때
When 사용자가 GET /api/v1/exams/history/{examId} 요청을 보내면
Then 시스템은 문제별 사용자 답안, 정답, 채점 결과를 포함한 HistoryDetailResult DTO를 반환하고
  And HTTP 200 응답을 반환한다
```

### Scenario 7.2: 미제출 시험 상세 이력 조회 시도
```gherkin
Given 인증된 사용자가 존재하고
  And 해당 사용자의 IN_PROGRESS 상태 시험이 존재할 때
When 사용자가 GET /api/v1/exams/history/{examId} 요청을 보내면
Then 시스템은 적절한 예외를 반환한다
```

### Scenario 7.3: 다른 사용자의 상세 이력 조회 시도
```gherkin
Given 인증된 사용자 A가 존재하고
  And 사용자 B의 SUBMITTED 상태 시험이 존재할 때
When 사용자 A가 GET /api/v1/exams/history/{examId} 요청을 보내면
Then 시스템은 EXAM_001 에러를 반환한다
```

## 8. Quality Gates

### Test Coverage
- 목표: 신규 서비스 코드 85% 이상 라인 커버리지
- 각 서비스별 최소 2개 이상의 테스트 시나리오 (정상 + 예외)
- ExamUseCase의 DTO 변환 정확성 검증

### Build Verification
- `./gradlew build` 성공
- `./gradlew test` 전체 통과
- 컴파일 에러 0건

### Architecture Compliance
- tmk-core 서비스에 Spring/JPA 의존성 없음
- DTO 변환이 tmk-api의 ExamUseCase에서만 수행됨
- Port 인터페이스를 통한 외부 통신만 사용

### Code Quality
- 모든 서비스에 @Service, @RequiredArgsConstructor 어노테이션 적용
- BusinessException(ErrorCode) 패턴 일관 사용
- 기존 코드 스타일(LoginService, QuestionUseCase) 준수

## 9. Definition of Done

- [ ] 6개 Core Service (GetExam, SaveAnswer, SubmitExam, GetExamResult, GetExamHistory, GetExamHistoryDetail)가 Stub에서 실제 로직으로 구현됨
- [ ] ExamUseCase의 7개 메서드에서 Entity -> DTO 변환 로직이 구현됨
- [ ] 각 서비스별 단위 테스트 작성 완료 (최소 2개 시나리오)
- [ ] `./gradlew build` 성공
- [ ] `./gradlew test` 전체 통과
- [ ] tmk-core에 외부 프레임워크 의존성 없음 확인
- [ ] 모든 엔드포인트가 정상 동작 (ExamController -> ExamUseCase -> Service 체인)
