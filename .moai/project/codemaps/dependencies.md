# TMK 외부 의존성 관리

## 모듈 간 의존성 그래프

### 의존성 방향

```
┌──────────────┐         ┌──────────────┐
│  tmk-batch   │         │   tmk-api    │
│ (배치 작업)  │         │ (REST API)   │
└──────┬───────┘         └────────┬─────┘
       │                          │
       │      모두 의존           │
       └──────────┬───────────────┘
                  ▼
           ┌─────────────┐
           │  tmk-core   │
           │ (도메인 로직) │
           │ (프레임워크    │
           │  독립적)      │
           └─────────────┘
                  │
                  │ 의존하지 않음
                  ▼
        (외부 프레임워크, DB 등)
```

### 상세 의존성 구조

```
tmk-api/build.gradle
├─ implementation(project(':tmk-core'))
├─ spring-boot-starter-web              # REST API
├─ spring-boot-starter-security         # 인증/권한
├─ spring-boot-starter-data-jpa         # ORM
├─ spring-boot-starter-data-redis       # 캐시
├─ postgresql                            # DB 드라이버
├─ org.springframework.ai:spring-ai-openai-spring-boot-starter
├─ org.pgvector:pgvector                # Vector 데이터 타입
└─ (기타 라이브러리)

tmk-batch/build.gradle
├─ implementation(project(':tmk-core'))
├─ spring-boot-starter-batch            # 배치 처리
├─ spring-boot-starter-data-jpa
├─ spring-boot-starter-data-redis
├─ postgresql
└─ (기타 라이브러리)

tmk-core/build.gradle
└─ testImplementation(...)               # 테스트만 포함
   (프로덕션 의존성 없음)
```

## 외부 라이브러리 목록

### Spring Boot Starter (Core)

| 라이브러리 | 버전 | 목적 | 사용 모듈 |
|-----------|------|------|----------|
| spring-boot-starter-web | 3.5.11 | REST API 및 웹 서버 | tmk-api |
| spring-boot-starter-security | 3.5.11 | 인증/권한 관리 | tmk-api |
| spring-boot-starter-data-jpa | 3.5.11 | ORM (Hibernate) | tmk-api, tmk-batch |
| spring-boot-starter-data-redis | 3.5.11 | Redis 캐시/세션 | tmk-api, tmk-batch |
| spring-boot-starter-batch | 3.5.11 | 배치 처리 | tmk-batch |
| spring-boot-starter-validation | 3.5.11 | 입력 검증 (@Valid) | tmk-api |

### 데이터베이스

| 라이브러리 | 버전 | 목적 | 설명 |
|-----------|------|------|------|
| postgresql | 42.x | JDBC 드라이버 | PostgreSQL 연결 |
| org.pgvector:pgvector | 0.x | 벡터 데이터 타입 | pgvector 네이티브 지원 |
| com.vladmihalcea:hibernate-types | 2.x | Hibernate 타입 확장 | JSON, 커스텀 타입 매핑 |

### AI/ML 통합

| 라이브러리 | 버전 | 목적 | 사용 사례 |
|-----------|------|------|----------|
| org.springframework.ai:spring-ai-openai | 1.x | OpenAI API 통합 | 임베딩, 질문 생성 |
| org.springframework.ai:spring-ai-core | 1.x | AI 프레임워크 기본 | LangChain 패턴 |

### 보안 및 토큰

| 라이브러리 | 버전 | 목적 | 사용 |
|-----------|------|------|------|
| io.jsonwebtoken:jjwt | 0.12.x | JWT 토큰 생성/검증 | 액세스 토큰, 리프레시 토큰 |
| io.jsonwebtoken:jjwt-jackson | 0.12.x | JWT와 Jackson 통합 | JSON 직렬화 |

### 데이터 처리

| 라이브러리 | 버전 | 목적 | 사용 |
|-----------|------|------|------|
| org.apache.pdfbox:pdfbox | 3.x | PDF 텍스트 추출 | 문서 처리 파이프라인 |
| org.jsoup:jsoup | 1.x | HTML/XML 파싱 | 텍스트 정제 |
| com.fasterxml.jackson.core | 2.x | JSON 직렬화 | API 응답 |

### 로깅 및 모니터링

| 라이브러리 | 버전 | 목적 | 사용 |
|-----------|------|------|------|
| org.springframework.boot:spring-boot-starter-logging | 3.5.11 | SLF4J + Logback | 애플리케이션 로그 |
| io.micrometer:micrometer-core | 1.x | 메트릭 수집 | 성능 모니터링 |

### 테스트

| 라이브러리 | 버전 | 목적 | 범위 |
|-----------|------|------|------|
| org.springframework.boot:spring-boot-starter-test | 3.5.11 | 통합 테스트 | 모든 모듈 |
| org.junit.jupiter:junit-jupiter | 5.x | JUnit 5 | 단위 테스트 |
| org.mockito:mockito-core | 5.x | 모킹 | 단위 테스트 |
| org.testcontainers:testcontainers | 1.x | 컨테이너 테스트 | PostgreSQL, Redis |

## 아키텍처별 의존성

### tmk-core (순수 도메인)

```gradle
dependencies {
    // 프로덕션 의존성 없음

    testImplementation {
        junit-jupiter
        mockito
        assertj
    }
}
```

**이유**: 도메인 로직이 외부 프레임워크에 의존하지 않음

### tmk-api (REST API)

```gradle
dependencies {
    // tmk-core 모듈
    implementation project(':tmk-core')

    // Spring Boot Starters
    spring-boot-starter-web          # REST, Tomcat
    spring-boot-starter-security     # 인증/권한
    spring-boot-starter-data-jpa     # Hibernate ORM
    spring-boot-starter-data-redis   # Redis 캐시
    spring-boot-starter-validation   # @Valid 검증

    // 데이터베이스
    postgresql                        # PostgreSQL JDBC
    pgvector                          # 벡터 타입

    // AI 통합
    spring-ai-openai-spring-boot-starter

    // 보안
    jjwt                             # JWT 토큰

    // 데이터 처리
    pdfbox                           # PDF 파싱
    jackson                          # JSON 직렬화

    // 테스트
    spring-boot-starter-test
    testcontainers-postgresql        # DB 테스트
    testcontainers-localstack        # AWS 서비스 테스트
}
```

### tmk-batch (배치 처리)

```gradle
dependencies {
    implementation project(':tmk-core')

    // Spring Boot Starters
    spring-boot-starter-batch       # Spring Batch
    spring-boot-starter-data-jpa    # ORM
    spring-boot-starter-data-redis  # 캐시

    // 데이터베이스
    postgresql

    // 테스트
    spring-boot-starter-test
    testcontainers-postgresql
}
```

## 버전 관리 전략

### Parent POM (build.gradle - 부모)

```gradle
ext {
    set('springCloudVersion', 'Spring cloud version')
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
    }
}
```

### 주요 버전

- **Java**: 21 (LTS)
- **Spring Boot**: 3.5.11
- **Gradle**: 8.x
- **PostgreSQL JDBC**: 42.7.x
- **OpenAI Spring AI**: 1.x (최신)
- **JWT (jjwt)**: 0.12.x (최신)

## 의존성 최소화 원칙

### 1. 계층별 명확한 분리

```
tmk-api
  ├─ 필요: Spring Web, Security, Data
  └─ 불필요: Spring Cloud, Kafka 등

tmk-batch
  ├─ 필요: Spring Batch, Data
  └─ 불필요: Spring Web, Security

tmk-core
  ├─ 필요: 테스트만
  └─ 불필요: 프로덕션 의존성 모두
```

### 2. 외부 라이브러리 최소화

**허용되는 의존성**:
- Spring Framework (비즈니스 구현에 필수)
- Lombok (코드 간결화)
- Validation API (입력 검증)

**금지되는 의존성**:
- Spring Cloud (비필요)
- Kafka (비즈니스 요구 아님)
- 특수 라이브러리 (대체 가능)

## 의존성 충돌 해결

### 버전 잠금 (Version Lock)

```gradle
ext {
    versions = [
        'spring-boot': '3.5.11',
        'postgresql': '42.7.1',
        'jjwt': '0.12.3'
    ]
}
```

### Exclude 규칙

```gradle
// 특정 라이브러리의 전이 의존성 제외
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-web') {
        exclude group: 'org.springframework.boot',
                module: 'spring-boot-starter-logging'
    }
}
```

## 성능 영향 분석

### 의존성 크기 최적화

| 라이브러리 | 크기 | 영향 | 최적화 |
|-----------|------|------|--------|
| spring-boot-starter-web | ~30MB | JAR 크기 | 필수 |
| spring-boot-starter-security | ~5MB | JAR 크기 | 필수 |
| spring-ai-openai | ~10MB | JAR 크기 | 필수 |
| spring-boot-starter-data-jpa | ~20MB | JAR 크기 | 필수 |

### 로드 시간 최적화

- **Lazy Initialization**: Spring Boot 3.5+에서 기본 지원
- **GraalVM Native Image**: 향후 고려

## 보안 업데이트 전략

### 정기 확인

```bash
# Gradle 의존성 보안 검사
./gradlew dependencyCheck

# Spring Boot 업그레이드 가이드
./gradlew -q dependencyUpdates
```

### 취약점 대응

1. **Critical (CVSS >= 9.0)**: 즉시 업그레이드
2. **High (7.0-8.9)**: 다음 마이너 릴리스에 포함
3. **Medium (4.0-6.9)**: 계획된 업그레이드 시기에
4. **Low (0-3.9)**: 모니터링만

## 라이센스 검증

### 허용된 라이센스

- Apache 2.0
- MIT
- GPL v2+ (with Classpath Exception)
- LGPL (라이브러리만)

### 검증 프로세스

```bash
# 라이센스 보고서 생성
./gradlew licenseReport
```

## 마이그레이션 로드맵

### 계획된 업그레이드

1. **Spring Boot 3.6.x** (2025년 상반기)
   - Virtual Threads 지원
   - Performance 개선

2. **Java 23** (선택적)
   - 최신 기능 활용

3. **Spring AI 1.1.x+**
   - 새로운 모델 지원
   - 성능 최적화

## 참고: build.gradle 최상위

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.11'
    id 'io.spring.dependency-management' version '1.1.x'
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    java {
        sourceCompatibility = '21'
    }

    repositories {
        mavenCentral()
    }
}
```
