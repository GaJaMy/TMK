---
id: SPEC-API-001
version: "1.0.0"
status: done
---

# SPEC-API-001: 구현 계획 - Swagger/OpenAPI 통합

## 개요

TMK 프로젝트에 Swagger/OpenAPI 문서화를 통합하여, 모든 REST API 엔드포인트를 대화형 문서로 제공한다. Swagger 어노테이션과 Spring MVC 어노테이션을 분리하는 인터페이스 패턴을 적용하여 관심사 분리를 달성한다.

## 마일스톤

### Primary Goal: springdoc-openapi 의존성 추가 및 SwaggerConfig 설정

**라이브러리 선택 근거:**

- `springdoc-openapi-starter-webmvc-ui:2.8.6` 선택
  - Spring Boot 3.x 공식 지원 (Jakarta EE 호환)
  - springfox 대비 활발한 유지보수 및 커뮤니티 지원
  - OpenAPI 3.0/3.1 네이티브 지원
  - Spring Boot auto-configuration 통한 간편한 설정
  - Swagger UI 내장 (별도 의존성 불필요)

**SwaggerConfig 설계:**

- `@Configuration` 클래스에 `OpenAPI` Bean 정의
- API 메타데이터 설정: 제목("TMK API"), 버전, 설명
- JWT Bearer 인증 스킴 구성:
  - SecurityScheme 타입: HTTP
  - Scheme: bearer
  - Bearer format: JWT
  - 이름: `bearerAuth`
- 전역 SecurityRequirement로 `bearerAuth` 적용

### Secondary Goal: ControllerDocs 인터페이스 생성

**인터페이스 분리 패턴 근거:**

- Controller 클래스의 가독성 유지 (Swagger 어노테이션으로 인한 코드 비대화 방지)
- 관심사 분리 원칙 준수 (문서화 vs 비즈니스 로직)
- Swagger 어노테이션 변경 시 Controller 로직에 영향 없음
- 인터페이스 기반이므로 IDE에서 문서 구조 일괄 확인 가능

**인터페이스별 구성:**

| 인터페이스 | 엔드포인트 | 주요 어노테이션 |
|-----------|-----------|----------------|
| `AuthControllerDocs` | 7개 | `@Tag("Auth")`, `@Operation`, `@ApiResponses` |
| `DocumentControllerDocs` | 2개 | `@Tag("Document - Internal")`, `@Operation`, `@ApiResponses` |
| `QuestionControllerDocs` | 2개 | `@Tag("Question")`, `@Operation`, `@ApiResponses` |
| `ExamControllerDocs` | 4+3개 | `@Tag("Exam")`, `@Operation`, `@ApiResponses` |

### Tertiary Goal: Request 클래스 및 Controller 통합

**Request 클래스 패키지 구조:**

- `auth.request`: 인증 관련 요청 DTO
  - 회원가입, 로그인, 인증코드 전송/검증 등의 요청 바디
  - `@Schema` 어노테이션으로 필드 설명 및 예시값 제공
- `document.request`: 문서 관련 요청 DTO
  - 문서 등록 요청 바디
  - `@Schema` 어노테이션 적용

**Controller 통합:**

- 각 Controller가 대응하는 Docs 인터페이스를 `implements`
- Controller에는 Spring MVC 어노테이션(`@GetMapping`, `@PostMapping` 등)만 유지
- Swagger 어노테이션은 Docs 인터페이스에서 상속

## 기술적 접근

### 의존성 구조

- `tmk-api/build.gradle`에만 springdoc 의존성 추가
- `tmk-core`에는 Swagger 관련 의존성 없음 (클린 아키텍처 원칙 유지)
- `tmk-batch`에는 Swagger 불필요 (배치 작업에 REST API 문서 불필요)

### 아키텍처 설계 방향

```
tmk-api/
├── config/
│   └── SwaggerConfig.java          # OpenAPI Bean, SecurityScheme
├── controller/
│   ├── docs/                       # ControllerDocs 인터페이스
│   │   ├── AuthControllerDocs.java
│   │   ├── DocumentControllerDocs.java
│   │   ├── QuestionControllerDocs.java
│   │   └── ExamControllerDocs.java
│   ├── AuthController.java         # implements AuthControllerDocs
│   ├── DocumentController.java     # implements DocumentControllerDocs
│   ├── QuestionController.java     # implements QuestionControllerDocs
│   └── ExamController.java         # implements ExamControllerDocs
└── controller/
    ├── auth/request/               # Auth 관련 요청 DTO
    └── document/request/           # Document 관련 요청 DTO
```

### 리스크 및 대응

| 리스크 | 영향도 | 대응 방안 |
|--------|--------|----------|
| springdoc 버전 호환성 | 중간 | Spring Boot 3.5.x 호환 버전(2.8.6) 확정 |
| 운영 환경 Swagger 노출 | 높음 | 프로파일별 활성화/비활성화 설정 필요 |
| 대규모 어노테이션 유지보수 | 낮음 | 인터페이스 분리로 변경 범위 최소화 |
| API 변경 시 문서 동기화 | 중간 | 컴파일 타임 인터페이스 검증으로 누락 방지 |

## 관련 SPEC

- SPEC-AUTH-001: 인증 관련 API 엔드포인트 정의 참조
- SPEC-REFACTOR-001: 클린 아키텍처 리팩토링 구조 참조
