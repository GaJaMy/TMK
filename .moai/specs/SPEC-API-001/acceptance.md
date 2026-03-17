---
id: SPEC-API-001
version: "1.0.0"
status: done
---

# SPEC-API-001: 인수 기준 - Swagger/OpenAPI 통합

## 시나리오 1: Swagger UI 접근성

```gherkin
Feature: Swagger UI 접근
  Swagger UI가 올바른 경로에서 접근 가능하고 모든 API가 표시되어야 한다

  Scenario: Swagger UI 페이지 로드
    Given 애플리케이션이 정상 실행 중이다
    When 브라우저에서 "/swagger-ui/index.html" 경로에 접근한다
    Then Swagger UI 페이지가 HTTP 200으로 응답한다
    And API 제목 "TMK API"가 페이지에 표시된다
    And API 버전 정보가 표시된다

  Scenario: OpenAPI JSON 스펙 접근
    Given 애플리케이션이 정상 실행 중이다
    When "/v3/api-docs" 경로에 GET 요청을 보낸다
    Then HTTP 200 응답과 함께 OpenAPI 3.0 JSON이 반환된다
    And JSON에 "paths" 객체가 포함되어 있다
    And JSON에 "components.securitySchemes.bearerAuth" 가 포함되어 있다
```

## 시나리오 2: API 그룹별 엔드포인트 문서화

```gherkin
Feature: 전체 API 엔드포인트 문서화
  모든 도메인의 API 엔드포인트가 Swagger UI에 표시되어야 한다

  Scenario: Auth API 엔드포인트 확인
    Given Swagger UI에 접근한다
    When "Auth" 태그 그룹을 펼친다
    Then 다음 7개 엔드포인트가 표시된다:
      | Method | Path                              | 설명               |
      | POST   | /api/v1/auth/email/send           | 인증코드 전송       |
      | POST   | /api/v1/auth/email/verify         | 인증코드 확인       |
      | POST   | /api/v1/auth/register             | 회원가입            |
      | POST   | /api/v1/auth/login                | 로그인              |
      | POST   | /api/v1/auth/logout               | 로그아웃            |
      | POST   | /api/v1/auth/reissue              | 토큰 재발급         |
      | POST   | /api/v1/auth/social               | 소셜 로그인         |

  Scenario: Document API 엔드포인트 확인
    Given Swagger UI에 접근한다
    When "Document" 태그 그룹을 펼친다
    Then 다음 2개 엔드포인트가 표시된다:
      | Method | Path                                      | 설명         |
      | POST   | /internal/v1/documents                    | 문서 등록     |
      | GET    | /internal/v1/documents/{documentId}/status | 상태 조회     |

  Scenario: Question API 엔드포인트 확인
    Given Swagger UI에 접근한다
    When "Question" 태그 그룹을 펼친다
    Then 다음 2개 엔드포인트가 표시된다:
      | Method | Path                              | 설명           |
      | GET    | /api/v1/questions                 | 문제 목록 조회  |
      | GET    | /api/v1/questions/{questionId}    | 문제 상세 조회  |

  Scenario: Exam API 엔드포인트 확인
    Given Swagger UI에 접근한다
    When "Exam" 태그 그룹을 펼친다
    Then 다음 7개 엔드포인트가 표시된다:
      | Method | Path                                    | 설명           |
      | POST   | /api/v1/exams                           | 시험 생성       |
      | GET    | /api/v1/exams/{examId}                  | 시험 조회       |
      | POST   | /api/v1/exams/{examId}/answers          | 답안 저장       |
      | POST   | /api/v1/exams/{examId}/submit           | 시험 제출       |
      | GET    | /api/v1/exams/{examId}/result           | 결과 조회       |
      | GET    | /api/v1/exams/history                   | 이력 목록       |
      | GET    | /api/v1/exams/history/{examId}          | 이력 상세       |
```

## 시나리오 3: JWT Bearer 인증 통합

```gherkin
Feature: Swagger UI JWT 인증
  Swagger UI에서 JWT 토큰을 입력하여 인증이 필요한 API를 테스트할 수 있어야 한다

  Scenario: JWT 토큰 입력 및 인증된 API 호출
    Given Swagger UI에 접근한다
    And 유효한 JWT access token을 보유하고 있다
    When "Authorize" 버튼을 클릭한다
    And "bearerAuth (http, Bearer)" 입력란에 JWT 토큰을 입력한다
    And "Authorize" 확인 버튼을 클릭한다
    Then 인증 상태가 "Authorized"로 변경된다
    When 인증 필요 API (예: POST /api/v1/exams)를 "Try it out"으로 실행한다
    Then 요청 헤더에 "Authorization: Bearer {token}"이 포함된다
    And API가 정상 응답을 반환한다

  Scenario: 미인증 상태에서 보호된 API 호출
    Given Swagger UI에 접근한다
    And "Authorize"를 수행하지 않았다
    When 인증 필요 API를 "Try it out"으로 실행한다
    Then 401 Unauthorized 응답이 반환된다
```

## 시나리오 4: ControllerDocs 인터페이스 분리 검증

```gherkin
Feature: ControllerDocs 인터페이스 분리 패턴
  Swagger 어노테이션과 Spring MVC 어노테이션이 올바르게 분리되어야 한다

  Scenario: ControllerDocs 인터페이스 존재 확인
    Given 프로젝트 소스 코드에 접근한다
    Then 다음 4개의 ControllerDocs 인터페이스가 존재한다:
      | 인터페이스                | 패키지 위치               |
      | AuthControllerDocs       | controller.docs 패키지    |
      | DocumentControllerDocs   | controller.docs 패키지    |
      | QuestionControllerDocs   | controller.docs 패키지    |
      | ExamControllerDocs       | controller.docs 패키지    |

  Scenario: Controller가 Docs 인터페이스를 구현
    Given 프로젝트 소스 코드에 접근한다
    Then 각 Controller가 대응하는 Docs 인터페이스를 implements 한다:
      | Controller             | 구현 인터페이스             |
      | AuthController         | AuthControllerDocs         |
      | DocumentController     | DocumentControllerDocs     |
      | QuestionController     | QuestionControllerDocs     |
      | ExamController         | ExamControllerDocs         |

  Scenario: 어노테이션 분리 원칙 준수
    Given ControllerDocs 인터페이스 소스 코드에 접근한다
    Then Docs 인터페이스에는 @Operation, @ApiResponse, @Tag 어노테이션이 존재한다
    And Docs 인터페이스에는 @GetMapping, @PostMapping 등 Spring MVC 어노테이션이 존재하지 않는다
    And Controller 클래스에는 @Operation, @ApiResponse 어노테이션이 존재하지 않는다
    And Controller 클래스에는 @GetMapping, @PostMapping 등 Spring MVC 어노테이션이 존재한다
```

## Quality Gate 기준

| 항목 | 기준 | 검증 방법 |
|------|------|----------|
| Swagger UI 접근 | `/swagger-ui/index.html` HTTP 200 | 브라우저 접근 테스트 |
| OpenAPI 스펙 | `/v3/api-docs` 유효한 JSON 반환 | curl 또는 HTTP 클라이언트 |
| 전체 엔드포인트 | 18개 엔드포인트 모두 문서화 | Swagger UI 목시 확인 |
| JWT 인증 | Authorize 기능 정상 동작 | 토큰 입력 후 API 호출 테스트 |
| 인터페이스 분리 | 4개 Docs 인터페이스 존재 | 소스 코드 구조 확인 |
| 어노테이션 분리 | Controller에 Swagger 어노테이션 없음 | 코드 리뷰 |

## Definition of Done

- [x] `springdoc-openapi-starter-webmvc-ui:2.8.6` 의존성이 `tmk-api/build.gradle`에 추가됨
- [x] `SwaggerConfig.java`에 OpenAPI Bean 및 JWT SecurityScheme 정의됨
- [x] 4개의 `XxxControllerDocs` 인터페이스가 생성됨
- [x] 4개의 Controller가 각각 대응하는 Docs 인터페이스를 구현함
- [x] Request 클래스에 `@Schema` 어노테이션이 적용됨
- [x] Swagger UI에서 전체 18개 엔드포인트가 확인 가능함
- [x] JWT Bearer 인증이 Swagger UI에서 동작함
