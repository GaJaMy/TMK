---
id: SPEC-REFACTOR-001
version: "1.0.0"
status: draft
created: "2026-03-12"
updated: "2026-03-12"
author: MoAI
priority: HIGH
issue_number: 0
---

# SPEC-REFACTOR-001: Clean Architecture Compliance Refactoring

## HISTORY

| Version | Date       | Author | Description                          |
|---------|------------|--------|--------------------------------------|
| 1.0.0   | 2026-03-12 | MoAI   | Initial SPEC creation                |

---

## 1. Environment (환경)

### 1.1 현재 시스템 상태

TMK 프로젝트는 멀티 모듈 구조(tmk-core, tmk-api, tmk-batch)를 갖추고 있으나, 핵심 도메인 모듈인 `tmk-core`가 Spring/JPA 프레임워크 의존성에 오염되어 Clean Architecture 원칙을 위반하고 있다.

### 1.2 위반 현황 요약

| 위반 유형                    | 건수 | 영향 범위                     |
|-----------------------------|------|------------------------------|
| JPA 어노테이션 오염 (@Entity, @Column) | 8개  | 전체 도메인 엔티티              |
| JpaRepository 확장           | 6개  | 전체 Repository 인터페이스      |
| @Service 어노테이션           | 23개 | 전체 도메인 서비스              |
| 프레임워크 설정 클래스 (Redis, JWT) | 3개  | tmk-core 모듈 내 위치          |
| UseCase 인터페이스 위치 오류    | N/A  | tmk-api에 존재 (tmk-core에 있어야 함) |
| UseCase 구현체 TODO 스텁       | N/A  | null 반환하는 미완성 구현체      |

### 1.3 기술 스택

- Java 21, Spring Boot 3.5.x, Gradle
- PostgreSQL + pgvector (vector(1536))
- Redis (이메일 인증, JWT refresh token, 시험 임시 답안)
- OpenAI API (text-embedding-3-small)

---

## 2. Assumptions (가정)

### 2.1 기술적 가정

- A1: tmk-core 모듈은 Spring Framework, JPA, Redis 등 외부 프레임워크에 대한 컴파일 타임 의존성 없이 빌드 가능해야 한다.
- A2: 도메인 엔티티와 JPA 엔티티를 분리하더라도 기존 비즈니스 로직의 동작은 변경되지 않아야 한다.
- A3: pgvector의 `vector(1536)` 타입은 도메인 계층에서 `float[]`로 표현 가능하다.
- A4: 리팩토링 과정에서 외부 API(REST 엔드포인트) 계약은 변경되지 않는다.
- A5: 기존 데이터베이스 스키마는 변경하지 않으며, JPA 엔티티 매핑만 tmk-api 어댑터 계층으로 이동한다.

### 2.2 조직적 가정

- A6: 리팩토링은 기능 추가 없이 구조 개선만 수행한다.
- A7: 기존 테스트가 존재하는 경우 리팩토링 후에도 동일하게 통과해야 한다.

---

## 3. Requirements (요구사항)

### 3.1 EARS 요구사항

#### REQ-EARS-001: Ubiquitous (항상 적용)

시스템은 **항상** 도메인 로직과 프레임워크 의존성 간의 엄격한 분리를 유지해야 한다.

> The system **shall** maintain strict separation between domain logic and framework dependencies.

- tmk-core 모듈의 `domain/` 패키지는 `jakarta.*`, `org.springframework.*`, `io.jsonwebtoken.*`, `org.springframework.data.*` 패키지를 import하지 않아야 한다.
- tmk-core 모듈의 `port/` 패키지는 순수 Java 인터페이스만 포함해야 한다.

#### REQ-EARS-002: Event-Driven (이벤트 기반)

**WHEN** tmk-core 모듈이 빌드될 때, **THEN** Spring Framework, JPA, Redis 의존성 없이 빌드가 성공해야 한다.

> **When** the application builds tmk-core module, the system **shall** succeed without Spring Framework, JPA, or Redis dependencies.

- `tmk-core/build.gradle`에서 `spring-boot-starter`, `spring-data-jpa`, `spring-data-redis`, `jjwt` 의존성이 제거되어야 한다.
- Gradle `dependencies` 블록에 프레임워크 관련 의존성이 존재하지 않아야 한다.

#### REQ-EARS-003: Unwanted Behavior (금지 동작)

**IF** tmk-core에 `@Entity`, `@Service`, `@Component`, `@Configuration` 어노테이션이 포함되어 있다면, **THEN** 시스템은 빌드에 실패해야 한다.

> **If** tmk-core contains any @Entity, @Service, @Component, or @Configuration annotations, the system **shall** fail the build.

- ArchUnit 또는 Gradle 커스텀 태스크를 통해 금지된 어노테이션 사용을 자동으로 감지하고 빌드를 실패시킨다.
- 금지 대상 어노테이션 목록:
  - `jakarta.persistence.Entity`
  - `jakarta.persistence.Table`
  - `jakarta.persistence.Column`
  - `jakarta.persistence.Id`
  - `jakarta.persistence.GeneratedValue`
  - `jakarta.persistence.ManyToOne`, `@OneToMany`, `@ManyToMany`
  - `org.springframework.stereotype.Service`
  - `org.springframework.stereotype.Component`
  - `org.springframework.context.annotation.Configuration`

#### REQ-EARS-004: State-Driven (상태 기반)

**IF** 도메인 서비스가 실행 중이라면, **THEN** 시스템은 Spring Application Context 없이 해당 서비스를 인스턴스화할 수 있어야 한다.

> **While** domain services are executed, the system **shall** instantiate them without requiring a Spring Application Context.

- 도메인 서비스는 생성자를 통해 Port 인터페이스를 주입받는 순수 POJO여야 한다.
- `new ExamCreationService(examPort, questionPort)` 형태로 직접 생성 가능해야 한다.
- Spring의 `@Autowired`, `@Inject` 어노테이션을 사용하지 않아야 한다.

#### REQ-EARS-005: Optional Feature (선택 기능)

**가능하면** pgvector 임베딩이 저장되는 경우, 도메인 객체에서는 `float[]` 타입을 사용하고, vector 직렬화는 어댑터 계층에서 처리해야 한다.

> **Where** pgvector embeddings are stored, the system **shall** use `float[]` type in domain objects and handle vector serialization in the adapter layer.

- `DocumentChunk` 도메인 객체의 `embedding` 필드는 `float[]` 타입이어야 한다.
- `DocumentChunkJpaEntity`의 어댑터에서 `float[]` <-> `vector(1536)` 변환을 처리한다.

---

## 4. Specifications (세부 명세)

### 4.1 Requirement Module

#### REQ-CORE-001: Domain Entity Purity (도메인 엔티티 순수성)

**목적**: tmk-core의 도메인 엔티티에서 `jakarta.persistence` 어노테이션을 완전히 제거한다.

**현재 상태**: 8개 도메인 엔티티가 `@Entity`, `@Column`, `@Id`, `@GeneratedValue` 등 JPA 어노테이션을 사용 중.

**목표 상태**:

| 도메인 엔티티            | 현재 (위반)                           | 목표 (순수 도메인)                    |
|------------------------|--------------------------------------|--------------------------------------|
| User                   | @Entity, @Column, @Id               | 순수 Java 클래스, 팩토리 메서드 패턴    |
| EmailVerification      | @Entity, @Column                     | 순수 Java 클래스, 불변 객체            |
| Document               | @Entity, @Column                     | 순수 Java 클래스                      |
| DocumentChunk          | @Entity, @Column, vector 타입        | 순수 Java 클래스, float[] embedding   |
| Question               | @Entity, @Column, @OneToMany         | 순수 Java 클래스, List<Option> 포함   |
| QuestionOption         | @Entity, @Column, @ManyToOne         | 순수 Java 클래스 (Value Object)       |
| Exam                   | @Entity, @Column                     | 순수 Java 클래스                      |
| ExamAnswer             | @Entity, @Column, @ManyToOne         | 순수 Java 클래스 (Value Object)       |

**변환 규칙**:
- `@Entity` 클래스 -> 순수 Java 클래스 (어노테이션 없음)
- `@Id` + `@GeneratedValue` -> `Long id` 필드 (어노테이션 없음)
- `@Column` -> 일반 필드 (어노테이션 없음)
- `@Enumerated(EnumType.STRING)` -> 일반 Enum 필드
- `@ManyToOne` / `@OneToMany` -> 일반 참조 또는 컬렉션

---

#### REQ-CORE-002: Repository Port Isolation (Repository 포트 격리)

**목적**: Repository 인터페이스가 `JpaRepository`를 확장하지 않는 순수 Java 인터페이스가 되어야 한다.

**현재 상태**: 6개 Repository 인터페이스가 `JpaRepository<Entity, Long>`을 확장하여 불필요한 JPA 메서드(flush, deleteInBatch 등)를 도메인 계층에 노출.

**목표 상태**:

```
// 현재 (위반)
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

// 목표 (순수 포트)
public interface UserPort {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
}
```

**포트 인터페이스 설계 원칙**:
- 도메인 서비스가 실제로 사용하는 메서드만 정의
- `JpaRepository`의 수십 개 메서드(flush, deleteAllInBatch 등) 노출 금지
- 네이밍 규칙: `{Domain}Port` (예: `UserPort`, `ExamPort`, `DocumentPort`)
- 위치: `tmk-core/port/out/` 패키지

---

#### REQ-CORE-003: Domain Service Independence (도메인 서비스 독립성)

**목적**: 도메인 서비스가 Spring Context 없이 인스턴스화 가능한 순수 POJO가 되어야 한다.

**현재 상태**: 23개 도메인 서비스가 `@Service` 어노테이션을 사용하며, Spring DI에 의존.

**목표 상태**:

```
// 현재 (위반)
@Service
@RequiredArgsConstructor
public class ExamCreationService {
    private final ExamRepository examRepository;
    ...
}

// 목표 (순수 POJO)
public class ExamCreationService {
    private final ExamPort examPort;
    private final QuestionPort questionPort;

    public ExamCreationService(ExamPort examPort, QuestionPort questionPort) {
        this.examPort = examPort;
        this.questionPort = questionPort;
    }
    ...
}
```

**변환 규칙**:
- `@Service` 제거
- `@RequiredArgsConstructor` -> 명시적 생성자 (또는 Lombok `@RequiredArgsConstructor` 유지하되 Spring 어노테이션 제거)
- Repository 타입을 Port 인터페이스로 교체
- `@Transactional` 제거 (트랜잭션 관리는 tmk-api 어댑터/Application Service에서 처리)

---

#### REQ-CORE-004: Configuration Layer Separation (설정 계층 분리)

**목적**: 프레임워크 설정 클래스를 tmk-core에서 tmk-api로 이동한다.

**현재 상태**: `RedisConfig`, `JwtProvider`, `JwtProperties`가 tmk-core에 존재.

**이동 대상**:

| 클래스            | 현재 위치    | 목표 위치    | 비고                           |
|------------------|------------|------------|-------------------------------|
| RedisConfig      | tmk-core   | tmk-api    | Redis 연결 설정                 |
| JwtProvider      | tmk-core   | tmk-api    | JWT 토큰 생성/검증               |
| JwtProperties    | tmk-core   | tmk-api    | JWT 설정값 바인딩                |

**추가 작업**:
- JWT 관련 도메인 로직이 있다면, 순수 인터페이스(Port)로 추출하여 tmk-core에 유지
- 예: `TokenPort` 인터페이스를 tmk-core에 정의, `JwtProvider`가 tmk-api에서 구현

---

#### REQ-API-001: Adapter Pattern Implementation (어댑터 패턴 구현)

**목적**: tmk-api가 Port 인터페이스를 전용 Adapter 클래스로 구현하고, JPA Entity를 도메인 Entity와 분리한다.

**구현 구조**:

```
tmk-api/
├── adapter/
│   └── out/
│       ├── persistence/
│       │   ├── entity/           # JPA 전용 엔티티
│       │   │   ├── UserJpaEntity.java
│       │   │   ├── ExamJpaEntity.java
│       │   │   └── ...
│       │   ├── repository/       # Spring Data JPA Repository
│       │   │   ├── UserJpaRepository.java   (extends JpaRepository)
│       │   │   └── ...
│       │   └── adapter/          # Port 구현체
│       │       ├── UserJpaAdapter.java       (implements UserPort)
│       │       └── ...
│       ├── redis/
│       │   └── adapter/
│       │       └── RedisTokenAdapter.java    (implements TokenPort)
│       └── jwt/
│           └── JwtProvider.java
```

**매퍼 패턴**:
- 각 어댑터는 도메인 엔티티 <-> JPA 엔티티 간 변환 메서드를 포함
- `toDomain(JpaEntity)`: JPA 엔티티를 도메인 객체로 변환
- `toJpaEntity(DomainEntity)`: 도메인 객체를 JPA 엔티티로 변환

**UseCase 인터페이스 이동**:
- 현재 tmk-api에 위치한 UseCase 인터페이스를 tmk-core/port/in/으로 이동
- UseCase 구현체(Application Service)는 tmk-api에 유지
- TODO 스텁으로 되어 있는 구현체는 실제 도메인 서비스 호출로 연결

---

## 5. Traceability (추적성)

| TAG ID             | 요구사항                      | 검증 방법                              |
|--------------------|-----------------------------|---------------------------------------|
| SPEC-REFACTOR-001  | Clean Architecture 준수      | ArchUnit 테스트, Gradle 의존성 검증      |
| REQ-CORE-001       | 도메인 엔티티 순수성            | JPA 어노테이션 부재 확인                  |
| REQ-CORE-002       | Repository 포트 격리          | JpaRepository 미확장 확인               |
| REQ-CORE-003       | 도메인 서비스 독립성            | Spring Context 없이 인스턴스화 테스트     |
| REQ-CORE-004       | 설정 계층 분리                 | tmk-core 내 설정 클래스 부재 확인         |
| REQ-API-001        | 어댑터 패턴 구현               | Adapter 클래스 존재 및 매핑 정확성 검증    |
