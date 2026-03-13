# Plan: SPEC-REFACTOR-001 - TMK 실무적 아키텍처 완성

## SPEC 후보

SPEC ID: SPEC-REFACTOR-001
Title: TMK 실무적 클린 아키텍처 완성 (TODO 구현 + Port 연결)
Domain: REFACTOR
Priority: HIGH

---

## 핵심 방침 (실무 허용 사항)

- ✅ **허용**: 도메인 엔티티에 @Entity, @Table 등 JPA 어노테이션 유지
- ✅ **허용**: tmk-core에 JpaRepository 유지
- ✅ **허용**: tmk-core에 spring-boot-starter-data-jpa 의존성 유지
- ✅ **허용**: @Service 어노테이션을 도메인 서비스에 추가 (Spring 자동 주입)
- ✅ **완료**: JWT/Redis 설정이 이미 tmk-api로 이동됨 (Phase 1 완료)

---

## 현재 상태 (Phase 1 완료 후)

| 구분 | 상태 | 비고 |
|------|------|------|
| Port/Out 인터페이스 6개 | ✅ 존재 | UserPort, ExamPort 등 |
| JpaRepository 6개 | ✅ 존재 | Port 인터페이스 미구현 상태 |
| 도메인 서비스 | ❌ TODO 스텁 | 전체 구현 필요 |
| UseCase DTO 변환 | ❌ null 반환 | 연결 필요 |
| DomainServiceConfig | ❌ 없음 | 생성 필요 |
| Batch 작업 | ❌ TODO 스텁 | 구현 필요 |
| JWT/Redis 설정 | ✅ tmk-api 이동 완료 | |

---

## 구현 단계별 계획

### Phase A: Repository → Port 인터페이스 연결

JpaRepository가 Port 인터페이스를 구현하도록 선언 추가.
메서드가 이미 동일하므로 `implements XxxPort` 한 줄씩 추가.

| 파일 | 변경 |
|------|------|
| `UserRepository` | `implements UserPort` 추가 |
| `ExamRepository` | `implements ExamPort` 추가 |
| `EmailVerificationRepository` | `implements EmailVerificationPort` 추가 + `deleteByEmail(String)` 메서드 추가 |
| `DocumentRepository` | `implements DocumentPort` 추가 |
| `DocumentChunkRepository` | `implements DocumentChunkPort` 추가 |
| `QuestionRepository` | `implements QuestionPort` 추가 (default 메서드 충돌 해결) |
| `ExamCreationService` | 의존성 `QuestionRepository` → `QuestionPort` 변경 |

**검증**: `./gradlew :tmk-core:compileJava` 성공

---

### Phase B: 도메인 서비스 @Service 추가 + 구현

@Service 추가로 Spring이 자동 빈 등록. 각 서비스 TODO 구현.

#### Auth 서비스

| 서비스 | 구현 내용 |
|--------|----------|
| `SendEmailVerificationService` | 6자리 랜덤 코드 생성, EmailVerification(5분 만료) 저장 (upsert: 기존 있으면 삭제 후 저장) |
| `VerifyEmailService` | findByEmail → 코드 일치 + 만료 확인 → verify() 호출 → 저장 |
| `RegisterUserService` | existsByEmail 중복 확인, BCrypt 비밀번호 해시, User.builder().build(), save |
| `LoginService` | findByEmail, BCryptPasswordEncoder.matches(), 불일치 시 예외 |
| `LogoutService` | @Service 추가, 비어있는 채로 유지 (Redis 블랙리스트는 UseCase 레이어에서 처리) |
| `ReissueTokenService` | @Service 추가, 비어있는 채로 유지 (Redis 조회는 UseCase 레이어에서 처리) |
| `SocialLoginService` | @Service 추가, 스텁 유지 (OAuth 미구현, 예외 throw) |

**Note**: BCryptPasswordEncoder는 `@Bean`으로 등록 필요 → DomainServiceConfig에서 처리

#### Document 서비스

| 서비스 | 구현 내용 |
|--------|----------|
| `RegisterDocumentService` | `DocumentRepository` → `DocumentPort`로 교체, Document(PENDING, now) 빌드 후 save |
| `GetDocumentStatusService` | `DocumentRepository` → `DocumentPort`로 교체, findById → NotFound 시 예외 |

#### Question 서비스

| 서비스 | 구현 내용 |
|--------|----------|
| `GetQuestionListService` | type/difficulty null 허용, findByTypeAndDifficulty 또는 findAll, 페이징 적용 |
| `GetQuestionDetailService` | findById → NotFound 시 예외 |

#### Exam 서비스

| 서비스 | 구현 내용 |
|--------|----------|
| `GetExamService` | findByIdAndUserId → NotFound 시 예외 |
| `SaveAnswerService` | findByIdAndUserId → exam.saveAnswer() 반복 → examPort.save(exam) |
| `SubmitExamService` | findExam → 만료/이미제출 확인 → QuestionPort로 문제 로드 → ExamGradingService.grade() → exam.submit() → save |
| `GetExamResultService` | findByIdAndUserId → 미제출 시 예외 |
| `GetExamHistoryService` | findByUserIdOrderByCreatedAtDesc → 페이징 |
| `GetExamHistoryDetailService` | findByIdAndUserId |

**SubmitExamService 의존성 추가**: `QuestionPort` 주입 추가

**검증**: `./gradlew :tmk-core:compileJava` 성공

---

### Phase C: DomainServiceConfig + BCryptPasswordEncoder

`tmk-api`에 `DomainServiceConfig` 생성:
- `BCryptPasswordEncoder @Bean` 등록 (LoginService, RegisterUserService에서 사용)
- @Service 어노테이션 덕분에 대부분 자동 등록 → 최소 설정만 필요

```java
@Configuration
public class DomainServiceConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

### Phase D: UseCase DTO 변환 완성

#### AuthUseCase 수정

현재 UseCase가 서비스 반환값을 무시하고 null 반환 → 실제 반환값 사용.
JwtProvider + RedisTemplate 의존성 추가.

| 메서드 | 구현 |
|--------|------|
| `register()` | RegisterUserService.register() 결과 무시 (void로 처리) |
| `login()` | LoginService.login() → User 반환 → JwtProvider.generateAccessToken/RefreshToken → Redis에 refresh token 저장 → LoginResult |
| `logout()` | JwtProvider.parseClaims() → Redis에 access token 블랙리스트 추가 |
| `reissue()` | Redis에서 refresh token 조회 → JwtProvider.generateAccessToken → ReissueResult |
| `socialLogin()` | SocialLoginService.socialLogin() → User → tokens → SocialLoginResult (스텁: 예외 throw) |

Redis key 규칙:
- Refresh token: `refresh:{userId}` → TTL = refreshTokenExpiry
- Access token 블랙리스트: `blacklist:{token}` → TTL = remaining access token expiry

#### ExamUseCase 수정

| 메서드 | 구현 |
|--------|------|
| `create()` | createExamService.create(userId) → Exam → ExamResult 변환 |
| `getExam()` | getExamService.getExam() → Exam → ExamDetailResult 변환 (questions 포함) |
| `saveAnswers()` | AnswerCommand → Map<Long, String> 변환 → saveAnswerService.saveAnswers() |
| `submit()` | submitExamService.submit() → Exam → SubmitResult 변환 |
| `getResult()` | getExamResultService.getResult() → Exam → ExamResultData 변환 |
| `getHistory()` | getExamHistoryService.getHistory() → List<Exam> → HistoryListResult 변환 |
| `getHistoryDetail()` | getExamHistoryDetailService.getHistoryDetail() → Exam → HistoryDetailResult 변환 |

ExamDetailResult 변환 시 Question 정보 필요 → QuestionPort 의존성 추가.

#### DocumentUseCase 수정

| 메서드 | 구현 |
|--------|------|
| `register()` | registerDocumentService.register() → Document → RegisterDocumentResult(id, title, status) |
| `getStatus()` | getDocumentStatusService.getStatus() → Document → DocumentStatusResult(id, title, status) |

#### QuestionUseCase 수정

| 메서드 | 구현 |
|--------|------|
| `getList()` | String → QuestionType/Difficulty enum 변환 (null 허용) → 서비스 호출 → QuestionListResult |
| `getDetail()` | getQuestionDetailService.getDetail() → Question → QuestionDetailResult (options 포함) |

**검증**: `./gradlew :tmk-api:compileJava` 성공

---

### Phase E: Batch 작업 구현

#### ExamAutoSubmitJob

```java
// 의존성 주입: SubmitExamService, ExamRepository (Port로)
// 로직: examRepository.findExpiredInProgressExams(now) → 각 exam에 대해 submit 호출
```

주의: Batch에서 도메인 서비스 사용 시 @Service가 있으므로 @Autowired 가능.

#### ExpiredVerificationCleanJob

```java
// 의존성 주입: EmailVerificationRepository
// 로직: 만료된 EmailVerification 삭제
```

EmailVerificationRepository에 `deleteByExpiredAtBefore(OffsetDateTime)` 메서드 추가 필요.

---

## 최종 검증

```bash
./gradlew build
```

전체 빌드 성공이 완료 기준.

---

## 변경 규모 (간소화)

| 구분 | 파일 수 |
|------|---------|
| tmk-core Repository 수정 (implements 추가) | 6개 |
| tmk-core Service 구현 | 16개 |
| tmk-api UseCase 수정 | 4개 |
| tmk-api Config 신규 | 1개 |
| tmk-batch 수정 | 2개 |
| **총계** | **29개** |

기존 계획(119개) 대비 75% 감소.
