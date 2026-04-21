# TMK (Test My Knowledge) API 명세서

> 작성일: 2026-03-10
> 버전: v1.0.0
> Base URL: `/api/v1`

---

## 공통 응답 형식

모든 API는 아래의 공통 응답 형식을 따릅니다.

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {}
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| errorCode | String | 처리 결과 코드. 성공 시 `SUCCESS` |
| msg | String | 처리 결과 메시지. 성공 시 `ok` |
| data | Object | 응답 데이터 |

---

## 에러 코드 정의

### 공통 (COMMON)

| errorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| SUCCESS | 200 | 요청 성공 |
| COMMON_001 | 400 | 잘못된 입력값 (유효성 검사 실패, 필수 파라미터 누락 등) |
| COMMON_002 | 500 | 서버 내부 오류 |

### 인증 (AUTH)

| errorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| AUTH_001 | 401 | 유효하지 않은 토큰 |
| AUTH_002 | 401 | 만료된 토큰 |
| AUTH_003 | 401 | 인증이 필요합니다 (토큰 없이 보호 API 접근) |
| AUTH_004 | 403 | 접근 권한이 없습니다 |
| AUTH_005 | 403 | 이메일 인증이 완료되지 않은 상태에서 회원가입 시도 |
| AUTH_006 | 401 | 이메일 또는 비밀번호 불일치 |
| AUTH_007 | 409 | 이미 사용 중인 이메일 |
| AUTH_008 | 400 | 이메일 인증 코드 불일치 또는 만료 |
| AUTH_009 | 401 | 유효하지 않은 리프레시 토큰 |

### 문서 (DOCUMENT)

| errorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| DOCUMENT_001 | 404 | 문서를 찾을 수 없음 |

### 문제 (QUESTION)

| errorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| QUESTION_001 | 404 | 문제를 찾을 수 없음 |
| QUESTION_002 | 422 | 시험을 생성할 문제가 부족함 |

### 시험 (EXAM)

| errorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| EXAM_001 | 404 | 시험을 찾을 수 없음 |
| EXAM_002 | 409 | 이미 제출된 시험 |
| EXAM_003 | 410 | 시험 시간 만료 |
| EXAM_004 | 400 | 진행 중인 시험이 아님 |

---

## 인증 방식

JWT 기반 인증을 사용합니다.
보호된 API는 요청 헤더에 액세스 토큰을 포함해야 합니다.

```
Authorization: Bearer {accessToken}
```

토큰 관련 에러는 필터 레벨에서 처리되며 아래와 같이 응답합니다.

```json
{
  "errorCode": "AUTH_001",
  "msg": "유효하지 않은 토큰입니다.",
  "data": null
}
```

---

## 1. 사용자 인증 API

> 회원가입 플로우: `이메일 인증 코드 발송` → `이메일 인증 코드 확인` → `회원가입`
> 이메일 인증이 완료되어야만 회원가입이 가능합니다.

---

### 1.1 이메일 인증 코드 발송

**POST** `/api/v1/auth/verification/send`

회원가입 전 이메일 인증 코드를 발송합니다. 인증 코드는 5분간 유효하며, 동일 이메일로 재발송 시 기존 코드는 덮어씁니다.

**Request Body**

```json
{
  "email": "user@example.com"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | Y | 인증할 이메일 |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": null
}
```

---

### 1.2 이메일 인증 코드 확인

**POST** `/api/v1/auth/verification/verify`

이메일로 수신한 인증 코드를 검증합니다. 인증 성공 시 해당 이메일은 회원가입 가능 상태가 됩니다.

**Request Body**

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | Y | 인증 대상 이메일 |
| code | String | Y | 이메일로 수신한 6자리 인증 코드 |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": null
}
```

**Error Response**

```json
{
  "errorCode": "AUTH_008",
  "msg": "인증 코드가 올바르지 않거나 만료되었습니다.",
  "data": null
}
```

---

### 1.3 회원가입

**POST** `/api/v1/auth/register`

이메일 인증 완료 후 이메일과 비밀번호로 회원가입을 완료합니다.
인증이 완료되지 않은 이메일로는 가입할 수 없습니다.

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "Password1234!"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | Y | 인증 완료된 이메일 |
| password | String | Y | 비밀번호 (영문, 숫자, 특수문자 포함 8자 이상) |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "email": "user@example.com"
  }
}
```

**Error Response**

```json
{
  "errorCode": "AUTH_005",
  "msg": "이메일 인증이 완료되지 않았습니다.",
  "data": null
}
```

---

### 1.4 로그인

**POST** `/api/v1/auth/login`

이메일과 비밀번호로 로그인합니다.

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "Password1234!"
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 1800
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | String | JWT 액세스 토큰 |
| refreshToken | String | JWT 리프레시 토큰 |
| expiresIn | Integer | 액세스 토큰 만료 시간 (초) |

**Error Response**

```json
{
  "errorCode": "AUTH_006",
  "msg": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

---

### 1.5 토큰 재발급

**POST** `/api/v1/auth/reissue`

리프레시 토큰으로 액세스 토큰을 재발급합니다.

**Request Body**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 1800
  }
}
```

**Error Response**

```json
{
  "errorCode": "AUTH_009",
  "msg": "유효하지 않은 리프레시 토큰입니다.",
  "data": null
}
```

---

### 1.6 로그아웃

**POST** `/api/v1/auth/logout`
🔒 인증 필요

**Request Header**

```
Authorization: Bearer {accessToken}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": null
}
```

---

### 1.7 소셜 로그인

**POST** `/api/v1/auth/social/{provider}`

소셜 로그인 제공자에서 받은 인가 코드를 전달하여 로그인합니다.

**Path Variable**

| 파라미터 | 설명 |
|----------|------|
| provider | 소셜 로그인 제공자 (`google`, `kakao`, `naver`) |

**Request Body**

```json
{
  "code": "authorization-code"
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 1800
  }
}
```

---

## 2. 문제 API

### 2.1 문제 목록 조회

**GET** `/api/v1/questions`
🔒 인증 필요

생성된 문제 목록을 조회합니다. 필터 및 페이지네이션을 지원합니다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| type | String | N | 문제 유형 (`MULTIPLE_CHOICE`, `FILL_IN_BLANK`, `IMPLEMENTATION`) |
| difficulty | String | N | 난이도 (`EASY`, `NORMAL`, `HARD`) |
| page | Integer | N | 페이지 번호 (기본값: 0) |
| size | Integer | N | 페이지 크기 (기본값: 20) |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "content": [
      {
        "questionId": 1,
        "content": "다음 중 Spring Boot의 특징으로 올바른 것은?",
        "type": "MULTIPLE_CHOICE",
        "difficulty": "EASY"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

### 2.2 문제 상세 조회

**GET** `/api/v1/questions/{questionId}`
🔒 인증 필요

특정 문제의 상세 정보를 조회합니다.

**Path Variable**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| questionId | Long | 문제 ID |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "questionId": 1,
    "content": "다음 중 Spring Boot의 특징으로 올바른 것은?",
    "type": "MULTIPLE_CHOICE",
    "difficulty": "EASY",
    "options": [
      { "number": 1, "content": "자동 설정(Auto Configuration)을 지원한다." },
      { "number": 2, "content": "내장 서버를 제공하지 않는다." },
      { "number": 3, "content": "XML 기반 설정만 지원한다." },
      { "number": 4, "content": "반드시 WAR 파일로 배포해야 한다." },
      { "number": 5, "content": "Spring Framework와 독립적으로 동작한다." }
    ],
    "answer": "1",
    "explanation": "Spring Boot는 자동 설정 기능을 제공하여 개발자가 별도의 설정 없이 빠르게 애플리케이션을 구동할 수 있도록 합니다."
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| questionId | Long | 문제 ID |
| content | String | 문제 내용 |
| type | String | 문제 유형 (`MULTIPLE_CHOICE`: 객관식, `FILL_IN_BLANK`: 빈칸 채우기, `IMPLEMENTATION`: 구현 문제) |
| difficulty | String | 난이도 (`EASY`: 쉬움, `NORMAL`: 보통, `HARD`: 어려움) |
| options | Array | 선택지 (객관식인 경우) |
| answer | String | 정답 |
| explanation | String | 해설 |

---

## 3. 시험 API

### 3.1 시험 생성

**POST** `/api/v1/exams`
🔒 인증 필요

생성된 문제를 기반으로 시험을 구성합니다. 시험은 최소 10문제로 구성되며 각 난이도별 최소 1문제가 포함됩니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "totalQuestions": 10,
    "timeLimit": 30,
    "startedAt": "2026-03-10 10:00:00",
    "expiredAt": "2026-03-10 10:30:00",
    "status": "IN_PROGRESS"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| examId | Long | 시험 ID |
| totalQuestions | Integer | 총 문제 수 |
| timeLimit | Integer | 시험 제한 시간 (분) |
| startedAt | String | 시험 시작 시각 (yyyy-MM-dd HH:mm:ss) |
| expiredAt | String | 시험 만료 시각 (yyyy-MM-dd HH:mm:ss) |
| status | String | 시험 상태 (`IN_PROGRESS`: 진행 중, `SUBMITTED`: 제출됨) |

---

### 3.2 시험 문제 목록 조회

**GET** `/api/v1/exams/{examId}`
🔒 인증 필요

진행 중인 시험의 문제 목록을 조회합니다. 정답과 해설은 포함되지 않습니다.

**Path Variable**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| examId | Long | 시험 ID |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "status": "IN_PROGRESS",
    "expiredAt": "2026-03-10 10:30:00",
    "questions": [
      {
        "questionId": 1,
        "order": 1,
        "content": "다음 중 Spring Boot의 특징으로 올바른 것은?",
        "type": "MULTIPLE_CHOICE",
        "difficulty": "EASY",
        "options": [
          { "number": 1, "content": "자동 설정(Auto Configuration)을 지원한다." },
          { "number": 2, "content": "내장 서버를 제공하지 않는다." },
          { "number": 3, "content": "XML 기반 설정만 지원한다." },
          { "number": 4, "content": "반드시 WAR 파일로 배포해야 한다." },
          { "number": 5, "content": "Spring Framework와 독립적으로 동작한다." }
        ],
        "myAnswer": null
      }
    ]
  }
}
```

---

### 3.3 답안 저장 / 수정

**PATCH** `/api/v1/exams/{examId}/answers`
🔒 인증 필요

시험 진행 중 답안을 저장하거나 수정합니다. 시험 시간 내에만 가능합니다.

**Path Variable**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| examId | Long | 시험 ID |

**Request Body**

```json
{
  "answers": [
    {
      "questionId": 1,
      "answer": "1"
    },
    {
      "questionId": 2,
      "answer": "Spring Bean은 기본적으로 싱글톤으로 관리된다."
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| answers | Array | Y | 답안 목록 |
| answers[].questionId | Long | Y | 문제 ID |
| answers[].answer | String | Y | 사용자 답안 |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": null
}
```

**Error Response**

```json
{
  "errorCode": "EXAM_003",
  "msg": "시험 시간이 만료되었습니다.",
  "data": null
}
```

---

### 3.4 시험 제출

**POST** `/api/v1/exams/{examId}/submit`
🔒 인증 필요

시험을 최종 제출합니다. 제출 후 자동으로 채점이 수행됩니다.

**Path Variable**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| examId | Long | 시험 ID |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "totalQuestions": 10,
    "correctCount": 7,
    "score": 70.0,
    "passed": true,
    "submittedAt": "2026-03-10 10:22:00"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| examId | Long | 시험 ID |
| totalQuestions | Integer | 총 문제 수 |
| correctCount | Integer | 정답 수 |
| score | Double | 정답률 (%) |
| passed | Boolean | 합격 여부 (정답률 50% 이상 시 `true`) |
| submittedAt | String | 제출 시각 (yyyy-MM-dd HH:mm:ss) |

**Error Response**

```json
{
  "errorCode": "EXAM_002",
  "msg": "이미 제출된 시험입니다.",
  "data": null
}
```

---

### 3.5 시험 결과 조회

**GET** `/api/v1/exams/{examId}/result`
🔒 인증 필요

제출된 시험의 결과를 조회합니다.

**Path Variable**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| examId | Long | 시험 ID |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "totalQuestions": 10,
    "correctCount": 7,
    "score": 70.0,
    "passed": true,
    "submittedAt": "2026-03-10 10:22:00"
  }
}
```

---

## 4. 시험 히스토리 API

### 4.1 시험 히스토리 목록 조회

**GET** `/api/v1/exams/history`
🔒 인증 필요

사용자의 시험 이력 목록을 조회합니다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| page | Integer | N | 페이지 번호 (기본값: 0) |
| size | Integer | N | 페이지 크기 (기본값: 20) |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "content": [
      {
        "examId": 101,
        "totalQuestions": 10,
        "correctCount": 7,
        "score": 70.0,
        "passed": true,
        "submittedAt": "2026-03-10 10:22:00"
      },
      {
        "examId": 99,
        "totalQuestions": 10,
        "correctCount": 4,
        "score": 40.0,
        "passed": false,
        "submittedAt": "2026-03-08 14:10:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 4.2 시험 히스토리 상세 조회

**GET** `/api/v1/exams/history/{examId}`
🔒 인증 필요

특정 시험의 상세 결과를 조회합니다. 각 문제별 내 답안, 정답, 해설이 포함됩니다.

**Path Variable**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| examId | Long | 시험 ID |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "totalQuestions": 10,
    "correctCount": 7,
    "score": 70.0,
    "passed": true,
    "submittedAt": "2026-03-10 10:22:00",
    "questions": [
      {
        "questionId": 1,
        "order": 1,
        "content": "다음 중 Spring Boot의 특징으로 올바른 것은?",
        "type": "MULTIPLE_CHOICE",
        "difficulty": "EASY",
        "options": [
          { "number": 1, "content": "자동 설정(Auto Configuration)을 지원한다." },
          { "number": 2, "content": "내장 서버를 제공하지 않는다." },
          { "number": 3, "content": "XML 기반 설정만 지원한다." },
          { "number": 4, "content": "반드시 WAR 파일로 배포해야 한다." },
          { "number": 5, "content": "Spring Framework와 독립적으로 동작한다." }
        ],
        "myAnswer": "1",
        "answer": "1",
        "explanation": "Spring Boot는 자동 설정 기능을 제공하여 개발자가 별도의 설정 없이 빠르게 애플리케이션을 구동할 수 있도록 합니다.",
        "isCorrect": true
      }
    ]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| questions[].myAnswer | String | 사용자가 제출한 답안 |
| questions[].answer | String | 정답 |
| questions[].explanation | String | 해설 |
| questions[].isCorrect | Boolean | 정답 여부 |

---

## 5. 내부 문서 관리 API (Internal)

> 외부에 공개되지 않는 내부 전용 API입니다. MVP에서는 관리자가 직접 호출하여 문서를 등록하며, 사용자는 접근할 수 없습니다.
> Base URL: `/internal/v1`

### 5.1 문서 등록 및 문제 생성 트리거

**POST** `/internal/v1/documents`

서버가 접근 가능한 파일 경로를 전달하여 문서를 등록하고 문제 생성 파이프라인을 실행합니다.
등록 후 아래 파이프라인이 비동기로 실행됩니다.

```
PDF 수신 → 텍스트 파싱 → 청킹 → OpenAI 임베딩 → pgvector 저장 → LLM 문제 생성
```

**Request Body** (`application/json`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | Y | 문서 제목 |
| source | String | Y | 서버 로컬 PDF 절대 경로 |

**Example**

```json
{
  "title": "Spring Boot 완전 정복",
  "source": "/absolute/path/spring-boot.pdf"
}
```

---

### 5.2 파일 업로드 등록

**POST** `/internal/v1/documents/upload`

PDF 파일을 업로드한 뒤 저장 경로를 기준으로 문서를 등록합니다.

**Request** (`multipart/form-data`)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| file | File | Y | PDF 파일 |
| title | String | Y | 문서 제목 |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "documentId": 1,
    "title": "Spring Boot 완전 정복",
    "status": "PROCESSING",
    "createdAt": "2026-03-10 10:00:00"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| documentId | Long | 등록된 문서 ID |
| title | String | 문서 제목 |
| status | String | 처리 상태 (`PROCESSING`: 파이프라인 실행 중) |
| createdAt | String | 등록 일시 (yyyy-MM-dd HH:mm:ss) |

---

### 5.3 문서 처리 상태 조회

**GET** `/internal/v1/documents/{documentId}/status`

문서 파이프라인 처리 상태를 확인합니다.

**Path Variable**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| documentId | Long | 문서 ID |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "documentId": 1,
    "title": "Spring Boot 완전 정복",
    "status": "COMPLETED",
    "chunkCount": 42,
    "questionCount": 10,
    "createdAt": "2026-03-10 10:00:00"
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| status | String | `PROCESSING`: 처리 중, `COMPLETED`: 완료, `FAILED`: 실패 |
| chunkCount | Integer | 생성된 청크 수 |
| questionCount | Integer | 생성된 문제 수 |

---

## API 목록 요약

| 번호 | Method | URL | 설명 | 인증 |
|------|--------|-----|------|------|
| 1 | POST | `/api/v1/auth/verification/send` | 이메일 인증 코드 발송 | ❌ |
| 2 | POST | `/api/v1/auth/verification/verify` | 이메일 인증 코드 확인 | ❌ |
| 3 | POST | `/api/v1/auth/register` | 회원가입 (인증 완료 후 가능) | ❌ |
| 4 | POST | `/api/v1/auth/login` | 로그인 | ❌ |
| 5 | POST | `/api/v1/auth/reissue` | 토큰 재발급 | ❌ |
| 6 | POST | `/api/v1/auth/logout` | 로그아웃 | ✅ |
| 7 | POST | `/api/v1/auth/social/{provider}` | 소셜 로그인 | ❌ |
| 8 | GET | `/api/v1/questions` | 문제 목록 조회 | ✅ |
| 9 | GET | `/api/v1/questions/{questionId}` | 문제 상세 조회 | ✅ |
| 10 | POST | `/api/v1/exams` | 시험 생성 | ✅ |
| 11 | GET | `/api/v1/exams/{examId}` | 시험 문제 목록 조회 | ✅ |
| 12 | PUT | `/api/v1/exams/{examId}/answers` | 답안 저장/수정 | ✅ |
| 13 | POST | `/api/v1/exams/{examId}/submit` | 시험 제출 | ✅ |
| 14 | GET | `/api/v1/exams/{examId}/result` | 시험 결과 조회 | ✅ |
| 15 | GET | `/api/v1/exams/history` | 시험 히스토리 목록 | ✅ |
| 16 | GET | `/api/v1/exams/history/{examId}` | 시험 히스토리 상세 | ✅ |
| 17 | POST | `/admin/v1/questions` | 관리자 공용 문제 직접 등록 | ✅ (ADMIN) |

> **내부 API (Internal) - 외부 공개 불가**

| 번호 | Method | URL | 설명 | 인증 |
|------|--------|-----|------|------|
| I-1 | POST | `/internal/v1/documents` | 경로 기반 문서 등록 및 문제 생성 트리거 | 내부 |
| I-2 | POST | `/internal/v1/documents/upload` | 파일 업로드 기반 문서 등록 | 내부 |
| I-3 | GET | `/internal/v1/documents/{documentId}/status` | 문서 처리 상태 조회 | 내부 |

---

## 웹 UI 진입점

현재 `tmk-api`에는 정적 웹 UI가 포함되어 있으며 기본 진입점은 다음과 같습니다.

| 페이지 | URL | 설명 |
|------|-----|------|
| 홈 | `/index.html` | 비로그인 시 로그인 화면, 로그인 후 제품 홈 |
| 시험 보기 | `/exams.html` | 실제 시험 응시 형태의 문제 풀이 UI |
| 문서 | `/documents.html` | 내부 문서 등록 및 상태 확인 |
| 문항 | `/questions.html` | 문제 목록/상세 탐색 |
| 인증 센터 | `/auth.html` | 이메일 인증, 회원가입, 재발급 |
