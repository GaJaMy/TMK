---
id: SPEC-BATCH-001
version: "1.0.0"
status: implemented
created: "2026-04-16"
updated: "2026-04-16"
author: GaJaMy
priority: high
issue_number: 0
lifecycle: spec-first
related_specs: [SPEC-EXAM-001, SPEC-AUTH-001]
---

# SPEC-BATCH-001: Batch Job Implementation (배치 작업 구현)

## HISTORY

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2026-04-16 | GaJaMy | Initial creation |

## 1. Environment

- **Project**: TMK (Test My Knowledge)
- **Module**: tmk-batch (Spring Batch), tmk-core (domain references)
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.5.11, Spring Batch
- **Database**: PostgreSQL
- **Architecture**: Clean Architecture - tmk-batch depends on tmk-core
- **Development Mode**: DDD (ANALYZE-PRESERVE-IMPROVE)

## 2. Assumptions

- **AS-1**: ExamPort.findExpiredInProgressExams(OffsetDateTime) is defined and implemented via ExamRepository with JPQL query.
- **AS-2**: ExamGradingService.grade(Exam, List<Question>) is fully implemented in tmk-core.
- **AS-3**: Exam.submit() changes status to SUBMITTED and sets submittedAt.
- **AS-4**: QuestionPort.findById(Long) is available.
- **AS-5**: EmailVerificationRepository.deleteByExpiredAtBefore(OffsetDateTime) is available via Spring Data JPA derived query.
- **AS-6**: TmkBatchApplication has scanBasePackages="com.tmk" but lacks @EnableJpaRepositories and @EntityScan for tmk-core entities.
- **AS-7**: SubmitExamService validates user ownership and expiry (throws exception for expired exams), so batch must bypass it and use ExamGradingService directly.

## 3. Requirements

### 3.1 Event-Driven Requirements

- **REQ-BATCH-001**: WHEN the ExamAutoSubmitJob scheduler triggers every 1 minute, THEN the system SHALL query all IN_PROGRESS exams with expiredAt before current time, grade each exam via ExamGradingService, call Exam.submit(), and save via ExamPort.
  - **Scope**: ExamAutoSubmitJob tasklet implementation + BatchScheduler with @Scheduled(fixedRate = 60000)

- **REQ-BATCH-002**: WHEN the ExpiredVerificationCleanJob scheduler triggers daily at 03:00, THEN the system SHALL delete all EmailVerification records where expiredAt is before the current time.
  - **Scope**: ExpiredVerificationCleanJob tasklet implementation + BatchScheduler with @Scheduled(cron = "0 0 3 * * *")

### 3.2 State-Driven Requirements

- **REQ-BATCH-003**: IF an exam has already been submitted (status != IN_PROGRESS) during batch processing, THEN the system SHALL skip that exam and continue processing remaining exams.

- **REQ-BATCH-004**: IF a question referenced by an ExamQuestion cannot be found, THEN the system SHALL skip grading that question (mark as incorrect) and continue.

### 3.3 Ubiquitous Requirements

- **REQ-BATCH-005**: The system SHALL log the count of auto-submitted exams after each ExamAutoSubmitJob execution.

- **REQ-BATCH-006**: The system SHALL log the count of deleted expired verifications after each ExpiredVerificationCleanJob execution.

### 3.4 Unwanted Requirements

- **REQ-BATCH-007**: The batch module SHALL NOT depend on tmk-api module or any web-layer components.

## 4. Specifications

### 4.1 TmkBatchApplication Changes

**File**: `tmk-batch/src/main/java/com/tmk/batch/TmkBatchApplication.java`

Add annotations:
- `@EnableScheduling` for @Scheduled support
- `@EnableJpaRepositories(basePackages = "com.tmk.core")` for JPA repository scanning
- `@EntityScan(basePackages = "com.tmk.core")` for JPA entity scanning

### 4.2 ExamAutoSubmitJob Tasklet

**File**: `tmk-batch/src/main/java/com/tmk/batch/job/ExamAutoSubmitJob.java`

Existing structure preserved (Job + Step beans). Tasklet implementation:

```
1. List<Exam> expiredExams = examPort.findExpiredInProgressExams(OffsetDateTime.now())
2. For each exam:
   a. Collect questionIds from exam.getExamQuestions()
   b. Fetch questions via questionPort.findById() for each id
   c. Call examGradingService.grade(exam, questions)
   d. Call exam.submit()
   e. Call examPort.save(exam)
   f. Catch exceptions per-exam (log and continue)
3. Log total processed count
4. Return RepeatStatus.FINISHED
```

Dependencies to inject: ExamPort, QuestionPort, ExamGradingService (already has ExamPort and SubmitExamService - replace SubmitExamService with QuestionPort and ExamGradingService)

### 4.3 ExpiredVerificationCleanJob Tasklet

**File**: `tmk-batch/src/main/java/com/tmk/batch/job/ExpiredVerificationCleanJob.java`

Existing structure preserved. Tasklet implementation:

```
1. Call emailVerificationRepository.deleteByExpiredAtBefore(OffsetDateTime.now())
2. Log deletion result
3. Return RepeatStatus.FINISHED
```

### 4.4 BatchScheduler (New)

**File**: `tmk-batch/src/main/java/com/tmk/batch/scheduler/BatchScheduler.java`

```
@Component
@RequiredArgsConstructor
public class BatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job examAutoSubmitJob;
    private final Job expiredVerificationCleanJob;

    @Scheduled(fixedRate = 60000)
    void runExamAutoSubmit() {
        // Launch examAutoSubmitJob with unique parameters (timestamp)
    }

    @Scheduled(cron = "0 0 3 * * *")
    void runExpiredVerificationClean() {
        // Launch expiredVerificationCleanJob with unique parameters (timestamp)
    }
}
```

## 5. Files to Create/Modify

| File | Action | Description |
|------|--------|-------------|
| TmkBatchApplication.java | MODIFY | Add @EnableScheduling, @EnableJpaRepositories, @EntityScan |
| ExamAutoSubmitJob.java | MODIFY | Implement tasklet with grading + submit logic |
| ExpiredVerificationCleanJob.java | MODIFY | Implement tasklet with delete logic |
| BatchScheduler.java | CREATE | New scheduler class with @Scheduled methods |

## 6. Risks and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Long-running exam auto-submit with many expired exams | Medium | Process individually with try-catch per exam |
| Concurrent job execution (overlapping schedules) | Low | Spring Batch prevents duplicate running instances via JobRepository |
| JPA LazyInitializationException in batch context | Medium | Ensure @Transactional on tasklet (PlatformTransactionManager provided by Step) |
