---
id: SPEC-REFACTOR-001
type: acceptance-criteria
version: "1.0.0"
created: "2026-03-12"
updated: "2026-03-12"
---

# SPEC-REFACTOR-001: Acceptance Criteria (인수 기준)

## 1. Test Scenarios (테스트 시나리오)

### Scenario 1: Core Module Purity (코어 모듈 순수성)

**Given**: tmk-core 모듈이 Clean Architecture 제약 조건으로 빌드되도록 구성되어 있다.
**When**: Gradle이 tmk-core를 컴파일한다.
**Then**: spring-framework, spring-data-jpa, spring-data-redis, jjwt 의존성이 tmk-core의 classpath에 존재하지 않은 상태에서 빌드가 성공한다.

**검증 방법**:
- `./gradlew :tmk-core:dependencies` 실행 시 `org.springframework`, `org.springframework.data`, `io.jsonwebtoken` 그룹 ID가 출력되지 않아야 한다.
- `./gradlew :tmk-core:build` 명령이 성공적으로 완료되어야 한다.

---

### Scenario 2: Domain Entity Framework Independence (도메인 엔티티 프레임워크 독립성)

**Given**: User 도메인 객체가 순수 Java 클래스로 정의되어 있다.
**When**: 테스트 환경에서 Spring ApplicationContext 없이 `new User(...)` 를 호출한다.
**Then**: User 객체가 성공적으로 생성되고, 모든 비즈니스 로직 메서드(예: `isLocalUser()`, `hasSocialProvider()`)가 정상적으로 동작한다.

**검증 방법**:
```java
@Test
void shouldCreateUserWithoutSpringContext() {
    // Given: 순수 Java 환경 (Spring Context 없음)
    // When
    User user = new User(1L, "test@example.com", "password123",
                         Provider.LOCAL, null, UserStatus.ACTIVE);
    // Then
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("test@example.com");
    assertThat(user.isLocalUser()).isTrue();
}
```

**추가 검증 대상 도메인 객체**:
- `EmailVerification`: 코드 만료 확인 로직 (`isExpired()`)
- `Document`: 상태 전이 로직 (`markAsProcessed()`)
- `Question`: 옵션 검증 로직 (MULTIPLE_CHOICE 시 5개 옵션 필수)
- `Exam`: 합격 판정 로직 (정답률 50% 이상)

---

### Scenario 3: Repository Port Minimalism (Repository 포트 최소주의)

**Given**: `UserPort` 인터페이스가 `tmk-core/port/out/` 패키지에 정의되어 있다.
**When**: 개발자가 `UserPort`의 메서드를 검사한다.
**Then**: 도메인 서비스가 실제로 사용하는 메서드만 존재하며 (`findByEmail`, `existsByEmail`, `save`), `JpaRepository`의 메서드 (`flush`, `deleteInBatch`, `findAll(Pageable)` 등)는 접근 불가능하다.

**검증 방법**:
```java
@Test
void userPortShouldNotExposeJpaRepositoryMethods() {
    // Given
    Class<?> userPortClass = UserPort.class;

    // When
    Method[] methods = userPortClass.getDeclaredMethods();
    List<String> methodNames = Arrays.stream(methods)
        .map(Method::getName)
        .toList();

    // Then
    assertThat(methodNames).contains("findByEmail", "existsByEmail", "save");
    assertThat(methodNames).doesNotContain("flush", "deleteInBatch",
        "deleteAllInBatch", "saveAndFlush", "getReferenceById");
}
```

**전체 포트 인터페이스 검증 대상**:

| Port 인터페이스      | 예상 메서드                                            |
|--------------------|----------------------------------------------------|
| UserPort           | findByEmail, existsByEmail, save                    |
| EmailVerificationPort | findByEmail, save, deleteByEmail                |
| DocumentPort       | findById, save, findByStatus                       |
| DocumentChunkPort  | saveAll, findByDocumentId, findSimilar             |
| QuestionPort       | findById, findByDocumentId, saveAll, findRandom    |
| ExamPort           | findById, save, findByUserId, findExpired          |

---

### Scenario 4: Adapter Layer Correctness (어댑터 계층 정확성)

**Given**: `UserJpaAdapter`가 tmk-api에서 Spring Bean으로 등록되어 있다.
**When**: `AuthApplicationService.login()`이 호출된다.
**Then**: `UserJpaAdapter.findByEmail()`이 호출되고, `UserJpaEntity`를 도메인 `User` 객체로 매핑하여 반환한다.

**검증 방법**:
```java
@SpringBootTest
class UserJpaAdapterIntegrationTest {

    @Autowired
    private UserPort userPort;  // UserJpaAdapter가 주입됨

    @Test
    void shouldMapJpaEntityToDomainUser() {
        // Given: DB에 사용자가 존재
        // When
        Optional<User> user = userPort.findByEmail("test@example.com");

        // Then
        assertThat(user).isPresent();
        assertThat(user.get()).isInstanceOf(User.class);
        // JpaEntity가 아닌 도메인 User 객체임을 확인
        assertThat(user.get().getClass().getAnnotation(Entity.class)).isNull();
    }
}
```

**매퍼 정확성 검증**:
- 도메인 -> JPA: 모든 필드가 정확히 매핑되는지 확인
- JPA -> 도메인: 모든 필드가 정확히 매핑되는지 확인
- Null 처리: nullable 필드의 올바른 처리
- 컬렉션 매핑: Question-Option, Exam-ExamAnswer 관계 매핑

---

### Scenario 5: UseCase Interface in Core (UseCase 인터페이스 코어 위치)

**Given**: `tmk-core/port/in/auth/LoginUseCase` 인터페이스가 존재한다.
**When**: `AuthController`가 `LoginUseCase` (인터페이스 타입)를 주입받는다.
**Then**: Spring이 `AuthApplicationService` (구현체)를 자동으로 주입하며, Controller는 구현체 클래스를 알지 못한다.

**검증 방법**:
```java
@SpringBootTest
class UseCaseWiringTest {

    @Autowired
    private LoginUseCase loginUseCase;

    @Test
    void shouldWireImplementationThroughInterface() {
        // Given: Spring Context가 초기화된 상태

        // Then: 인터페이스를 통해 구현체가 주입됨
        assertThat(loginUseCase).isNotNull();
        // Controller는 구현체 클래스명을 모른 채 인터페이스로만 사용
    }
}
```

**UseCase 인터페이스 이동 검증 대상**:

| UseCase 인터페이스              | 예상 위치 (목표)                        |
|-------------------------------|---------------------------------------|
| LoginUseCase                  | tmk-core/port/in/auth/                |
| RegisterUseCase               | tmk-core/port/in/auth/                |
| SendEmailVerificationUseCase  | tmk-core/port/in/auth/                |
| VerifyEmailUseCase            | tmk-core/port/in/auth/                |
| RegisterDocumentUseCase       | tmk-core/port/in/document/            |
| GetQuestionListUseCase        | tmk-core/port/in/question/            |
| CreateExamUseCase             | tmk-core/port/in/exam/                |
| SubmitExamUseCase             | tmk-core/port/in/exam/                |

---

## 2. Edge Cases (경계 사례)

### Edge Case 1: pgvector Embedding 변환

**Given**: `DocumentChunk` 도메인 객체의 embedding 필드가 `float[]` 타입이다.
**When**: `DocumentChunkJpaAdapter`가 도메인 객체를 JPA 엔티티로 변환한다.
**Then**: `float[]`이 `vector(1536)` 타입으로 정확히 변환되고, 역변환 시 데이터 손실이 없다.

### Edge Case 2: Enum 매핑 일관성

**Given**: 도메인 엔티티에서 `Provider`, `UserStatus`, `QuestionType` 등의 Enum을 사용한다.
**When**: JPA 어댑터가 도메인 Enum을 DB VARCHAR로 변환한다.
**Then**: 도메인 Enum 값과 DB 저장 값이 정확히 일치하며, 알 수 없는 값에 대해 적절한 예외를 발생시킨다.

### Edge Case 3: 양방향 관계 매핑

**Given**: `Question`은 `List<QuestionOption>`을, `Exam`은 `List<ExamAnswer>`를 포함한다.
**When**: JPA 어댑터가 관계를 포함한 엔티티를 도메인 객체로 변환한다.
**Then**: 컬렉션이 올바르게 변환되며, Lazy Loading 예외가 발생하지 않는다.

### Edge Case 4: TODO 스텁 UseCase 구현체

**Given**: 일부 UseCase 구현체가 `null`을 반환하는 TODO 스텁 상태이다.
**When**: 리팩토링 후 해당 UseCase가 호출된다.
**Then**: 실제 도메인 서비스 로직이 실행되거나, 미구현 상태임을 명시적으로 알리는 `UnsupportedOperationException`이 발생한다.

### Edge Case 5: 트랜잭션 경계 이동

**Given**: 기존 `@Transactional`이 도메인 서비스에 위치해 있었다.
**When**: 트랜잭션 관리가 tmk-api Application Service로 이동한다.
**Then**: 기존과 동일한 트랜잭션 범위가 유지되며, 데이터 정합성이 보장된다.

---

## 3. Performance Criteria (성능 기준)

| 항목                          | 기준                                        |
|------------------------------|---------------------------------------------|
| tmk-core 빌드 시간             | 리팩토링 전 대비 동등하거나 빠른 빌드 시간       |
| API 응답 시간                  | 리팩토링 전과 동일한 P95 응답 시간 유지          |
| 메모리 사용량                   | 도메인-JPA 엔티티 변환으로 인한 추가 메모리 5% 이내 |
| 매퍼 변환 오버헤드              | 단일 엔티티 변환 1ms 이하                      |
| 전체 테스트 실행 시간           | 리팩토링 전 대비 10% 이내 증가                  |

---

## 4. Quality Gate Criteria (품질 게이트 기준)

### 4.1 TRUST 5 Framework 적용

| Pillar      | 기준                                                        | 검증 도구          |
|-------------|------------------------------------------------------------|--------------------|
| Tested      | tmk-core 도메인 로직 단위 테스트 85%+ 커버리지                  | JaCoCo             |
| Readable    | 명확한 패키지 구조, 일관된 네이밍 (Port, Adapter, JpaEntity)    | 코드 리뷰          |
| Unified     | 전체 모듈 일관된 코드 스타일                                    | Checkstyle         |
| Secured     | 프레임워크 분리로 인한 보안 취약점 없음                           | ArchUnit           |
| Trackable   | Conventional Commits, SPEC 참조 포함                          | Git hooks          |

### 4.2 Architecture Compliance (아키텍처 준수)

- [ ] tmk-core에 `jakarta.persistence` import 0건
- [ ] tmk-core에 `org.springframework` import 0건
- [ ] tmk-core에 `io.jsonwebtoken` import 0건
- [ ] tmk-core에 `org.springframework.data` import 0건
- [ ] 모든 Repository 인터페이스가 `JpaRepository`를 확장하지 않음
- [ ] 모든 도메인 서비스가 `@Service` 어노테이션 없음
- [ ] `RedisConfig`, `JwtProvider`, `JwtProperties`가 tmk-api에 위치
- [ ] 모든 UseCase 인터페이스가 tmk-core/port/in/에 위치
- [ ] 모든 UseCase 구현체가 TODO 스텁이 아닌 실제 로직 연결

### 4.3 Regression Verification (회귀 검증)

- [ ] 기존 API 엔드포인트가 동일한 요청/응답 계약을 유지
- [ ] 기존 통합 테스트가 모두 통과
- [ ] 데이터베이스 스키마 변경 없음
- [ ] Batch Job (`ExamAutoSubmitJob`, `ExpiredVerificationCleanJob`) 정상 동작

---

## 5. Definition of Done (완료 정의)

이 SPEC의 리팩토링이 완료된 것으로 간주되려면 다음 모든 조건이 충족되어야 한다:

1. **빌드 성공**: `./gradlew :tmk-core:build`가 프레임워크 의존성 없이 성공
2. **아키텍처 테스트 통과**: ArchUnit 기반 아키텍처 제약 테스트 전체 통과
3. **단위 테스트 통과**: 도메인 로직 단위 테스트 85%+ 커버리지
4. **통합 테스트 통과**: 기존 API 통합 테스트 전체 통과
5. **매핑 정확성**: 도메인-JPA 엔티티 간 매핑 테스트 통과
6. **회귀 없음**: 기존 기능 동작에 변경 없음
7. **코드 리뷰**: Architecture Compliance 체크리스트 전체 항목 충족
