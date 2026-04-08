# Research: SPEC-EXAM-001 - Exam Feature Implementation

## Architecture Summary

- Spring Boot 3.5.11 + Java 21 LTS
- Clean Architecture: tmk-core (domain) -> tmk-api (adapters) -> tmk-batch (batch)
- PostgreSQL + JPA, Redis for caching, JWT authentication

## Current State

### Completed Components
| Component | File | Status |
|-----------|------|--------|
| Exam Entity | `tmk-core/.../exam/entity/Exam.java` | Complete (submit(), saveAnswer(), isExpired()) |
| ExamQuestion Entity | `tmk-core/.../exam/entity/ExamQuestion.java` | Complete (saveAnswer(), grade()) |
| ExamStatus Enum | `tmk-core/.../exam/entity/ExamStatus.java` | Complete (IN_PROGRESS, SUBMITTED, EXPIRED) |
| ExamCreationService | `tmk-core/.../exam/service/ExamCreationService.java` | Complete (question selection algorithm) |
| CreateExamService | `tmk-core/.../exam/service/CreateExamService.java` | Complete (orchestrates creation + persistence) |
| ExamGradingService | `tmk-core/.../exam/service/ExamGradingService.java` | Complete (answer comparison, grading) |
| ExamPort | `tmk-core/.../port/out/ExamPort.java` | Complete (findById, findByIdAndUserId, save, etc.) |
| QuestionPort | `tmk-core/.../port/out/QuestionPort.java` | Complete (findGroupedByDifficulty, findAll, etc.) |
| ExamRepository | `tmk-core/.../exam/repository/ExamRepository.java` | Complete (JPA, implements ExamPort) |
| ExamController | `tmk-api/.../exam/controller/ExamController.java` | Complete (all 7 endpoints defined) |
| ExamControllerDocs | `tmk-api/.../exam/controller/ExamControllerDocs.java` | Complete (Swagger docs) |
| All DTOs | `tmk-api/.../exam/dto/` | Complete (ExamResult, ExamDetailResult, SubmitResult, etc.) |
| ErrorCodes | `tmk-core/.../exception/ErrorCode.java` | Complete (EXAM_001~004, QUESTION_002) |
| SecurityConfig | `tmk-api/.../config/SecurityConfig.java` | Complete (/api/** requires auth) |

### Stub Components (TODO)
| Component | File | Needed Implementation |
|-----------|------|----------------------|
| GetExamService | `tmk-core/.../exam/service/GetExamService.java` | Ownership-validated exam retrieval |
| SaveAnswerService | `tmk-core/.../exam/service/SaveAnswerService.java` | Answer persistence with validation |
| SubmitExamService | `tmk-core/.../exam/service/SubmitExamService.java` | Grade + submit orchestration |
| GetExamResultService | `tmk-core/.../exam/service/GetExamResultService.java` | Submitted exam result retrieval |
| GetExamHistoryService | `tmk-core/.../exam/service/GetExamHistoryService.java` | Paginated exam history |
| GetExamHistoryDetailService | `tmk-core/.../exam/service/GetExamHistoryDetailService.java` | Detailed history with answers |
| ExamUseCase | `tmk-api/.../exam/usecase/ExamUseCase.java` | 7 methods DTO conversion |

## Reference Implementations

### Service Pattern (LoginService)
```
@Service @RequiredArgsConstructor
- Port injection
- BusinessException(ErrorCode.X) for domain errors
- Return entity (UseCase handles DTO conversion)
```

### UseCase DTO Pattern (QuestionUseCase)
```
1. Call core service -> get entity
2. Map nested objects with streams
3. Construct result DTO
4. Return to controller
```

### Test Pattern (VerifyEmailServiceTest)
```
@ExtendWith(MockitoExtension.class)
- @Mock ports
- @InjectMocks service
- Arrange/Act/Assert with AssertJ
- verify() port interactions
```

## Business Rules
- Minimum 10 questions per exam
- At least 1 question per difficulty (EASY, MEDIUM, HARD)
- Default time limit: 30 minutes
- Pass threshold: 50% correct
- Exam ownership: user can only access own exams
- Status transitions: IN_PROGRESS -> SUBMITTED or EXPIRED
- Submitted exams cannot be modified

## Risks
1. No concurrency control on exam submission (race condition)
2. QuestionPort.findGroupedByDifficulty() loads ALL questions into memory
3. No @Valid annotations on AnswerCommand DTO
4. ExamQuestion.questionId is not a FK (deleted question risk)

## Quality Baseline
- Build: Passes cleanly
- Test coverage: 1.6% (2 meaningful tests, 0 exam tests)
- Development mode: DDD (ANALYZE-PRESERVE-IMPROVE)
- No CI/CD, no code quality tools configured
