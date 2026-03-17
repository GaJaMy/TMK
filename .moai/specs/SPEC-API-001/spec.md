---
id: SPEC-API-001
version: "1.0.0"
status: done
created: "2026-03-17"
updated: "2026-03-17"
author: MoAI
priority: medium
issue_number: 0
---

# SPEC-API-001: Swagger/OpenAPI 통합

## Environment (환경)

- **프로젝트**: TMK (Test My Knowledge) - AI 기반 문제은행 플랫폼
- **프레임워크**: Spring Boot 3.5.x, Java 21
- **모듈 구조**: 멀티 모듈 (tmk-core, tmk-api, tmk-batch)
- **인증 방식**: JWT Bearer Token (`Authorization: Bearer {accessToken}`)
- **API 기본 경로**: `/api/v1` (공개), `/internal/v1` (내부 전용)
- **도메인**: Auth, Document, Question, Exam

## Assumptions (가정)

- springdoc-openapi 라이브러리가 Spring Boot 3.5.x와 호환된다
- Swagger UI는 개발 및 테스트 환경에서 주로 사용되며, 운영 환경에서는 비활성화할 수 있다
- 각 Controller는 단일 Docs 인터페이스를 구현하여 Swagger 어노테이션과 Spring MVC 어노테이션을 분리한다
- JWT Bearer 인증 스킴이 Swagger UI의 "Authorize" 버튼을 통해 전역으로 적용된다
- API 응답 형식은 기존 `ApiResponse` 공통 응답 래퍼를 따른다

## Requirements (요구사항)

### Ubiquitous (항상 활성)

- REQ-API-001: 시스템은 **항상** `/swagger-ui/index.html` 경로에서 Swagger UI를 제공해야 한다
- REQ-API-002: 시스템은 **항상** `/v3/api-docs` 경로에서 OpenAPI 3.0 JSON 스펙을 제공해야 한다
- REQ-API-003: 시스템은 **항상** Swagger 어노테이션을 `XxxControllerDocs` 인터페이스에 분리하여 관리해야 한다

### Event-Driven (이벤트 기반)

- REQ-API-004: **WHEN** 사용자가 Swagger UI에서 "Authorize" 버튼을 클릭하고 JWT 토큰을 입력 **THEN** 이후 모든 API 요청에 `Authorization: Bearer {token}` 헤더가 자동으로 포함되어야 한다
- REQ-API-005: **WHEN** 애플리케이션이 시작 **THEN** `SwaggerConfig`에 정의된 OpenAPI Bean이 로드되어 API 문서 메타데이터(제목, 버전, 설명)와 JWT 보안 스킴이 설정되어야 한다

### State-Driven (상태 기반)

- REQ-API-006: **IF** Controller 클래스가 `XxxControllerDocs` 인터페이스를 구현 **THEN** 해당 Controller의 모든 엔드포인트가 Swagger UI에 문서화되어야 한다
- REQ-API-007: **IF** 엔드포인트가 인증이 필요한 경우 **THEN** Swagger UI에서 자물쇠 아이콘이 표시되어야 한다

### Unwanted (금지)

- REQ-API-008: Controller 클래스에 직접 `@Operation`, `@ApiResponse` 등 Swagger 어노테이션을 **배치하지 않아야 한다** (ControllerDocs 인터페이스에만 배치)
- REQ-API-009: Spring MVC 어노테이션(`@GetMapping`, `@PostMapping` 등)을 ControllerDocs 인터페이스에 **배치하지 않아야 한다**

## Specifications (세부 사양)

### API 그룹별 문서화 범위

| 도메인 | Docs 인터페이스 | Controller | 엔드포인트 수 |
|--------|----------------|------------|--------------|
| Auth | `AuthControllerDocs` | `AuthController` | 7개 (인증코드 전송, 인증, 회원가입, 로그인, 로그아웃, 토큰 재발급, 소셜 로그인) |
| Document | `DocumentControllerDocs` | `DocumentController` | 2개 (문서 등록, 상태 조회 - 내부 API) |
| Question | `QuestionControllerDocs` | `QuestionController` | 2개 (문제 목록 조회, 문제 상세 조회) |
| Exam | `ExamControllerDocs` | `ExamController` | 7개 (시험 생성, 시험 조회, 답안 저장, 시험 제출, 결과 조회, 이력 목록, 이력 상세) |

### SwaggerConfig 구성

- OpenAPI Bean 정의: API 제목, 버전, 설명 포함
- SecurityScheme: `bearerAuth` 이름의 HTTP Bearer JWT 스킴
- SecurityRequirement: 전역 보안 요구사항으로 `bearerAuth` 적용

### ControllerDocs 인터페이스 패턴

```
// 패턴 설명 (코드가 아닌 구조 설명)
XxxControllerDocs 인터페이스:
  - @Tag: API 그룹 이름 및 설명
  - 각 메서드: @Operation(summary, description)
  - 각 메서드: @ApiResponses({@ApiResponse(responseCode, description)})
  - 각 메서드: @Parameter (필요시)

XxxController (구현):
  - implements XxxControllerDocs
  - Spring MVC 어노테이션만 포함 (@GetMapping, @PostMapping 등)
  - 비즈니스 로직은 UseCase 호출
```

### Request 클래스 구조

- `auth.request` 패키지: 인증 관련 요청 DTO
- `document.request` 패키지: 문서 관련 요청 DTO
- 각 Request 클래스에 `@Schema` 어노테이션으로 필드 설명 포함

## Traceability (추적성)

| 요구사항 ID | 관련 파일 | 검증 방법 |
|------------|----------|----------|
| REQ-API-001 | `SwaggerConfig.java` | Swagger UI 접근 테스트 |
| REQ-API-002 | `SwaggerConfig.java` | API docs 엔드포인트 응답 확인 |
| REQ-API-003 | `*ControllerDocs.java` | 인터페이스 분리 구조 검증 |
| REQ-API-004 | `SwaggerConfig.java` | JWT 인증 후 API 호출 테스트 |
| REQ-API-005 | `SwaggerConfig.java` | 애플리케이션 시작 시 설정 로드 확인 |
| REQ-API-006 | `*Controller.java` | 각 Controller의 Docs 인터페이스 구현 확인 |
| REQ-API-007 | `SwaggerConfig.java` | 인증 필요 엔드포인트 자물쇠 아이콘 확인 |
| REQ-API-008 | `*Controller.java` | Controller에 Swagger 어노테이션 부재 확인 |
| REQ-API-009 | `*ControllerDocs.java` | Docs 인터페이스에 Spring MVC 어노테이션 부재 확인 |
