# TMK (Test My Knowledge) API 명세서

> 작성일: 2026-04-27
> 버전: v2.0.0
> 사용자 인증 Base URL: `/api/auth/v1`
> 사용자 기능 Base URL: `/`
> 관리자 Base URL: `/admin/v1`

---

이 문서의 관리자 API는 별도 서버가 아니라 `tmk-api` 단일 서버 내부의 관리자 패키지/도메인을 통해 `/admin/v1/**` 경로로 제공되는 구조를 전제로 합니다.

현재 구현 원칙상 컨트롤러는 자원 경로를 상위 `@RequestMapping`에 두고, 버전은 메서드 레벨에서 `ApiVersion.V1`, `ApiVersion.V2` 상수를 사용해 관리합니다.

---

## 공통 응답 형식

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {}
}
```

---

## 인증 방식

JWT Bearer Token

```text
Authorization: Bearer {accessToken}
```

---

## 에러 코드

### AUTH

| 코드 | HTTP | 설명 |
|------|------|------|
| AUTH_001 | 401 | 유효하지 않은 액세스 토큰 |
| AUTH_002 | 401 | 만료된 액세스 토큰 |
| AUTH_003 | 401 | 유효하지 않은 리프레시 토큰 |
| AUTH_004 | 401 | 인증 필요 |
| AUTH_005 | 403 | 권한 없음 |
| AUTH_006 | 409 | 이미 사용 중인 아이디 |
| AUTH_007 | 401 | 아이디 또는 비밀번호 불일치 |
| AUTH_008 | 404 | 사용자 계정을 찾을 수 없음 |
| AUTH_009 | 404 | 관리자 계정을 찾을 수 없음 |
| AUTH_010 | 403 | 비활성 사용자 계정 |
| AUTH_011 | 403 | 비활성 관리자 계정 |
| AUTH_012 | 404 | 비밀번호 재설정 대상 계정을 찾을 수 없음 |

### TOPIC

| 코드 | HTTP | 설명 |
|------|------|------|
| TOPIC_001 | 404 | Topic을 찾을 수 없음 |
| TOPIC_002 | 409 | 이미 존재하는 Topic 이름 |
| TOPIC_003 | 409 | 공용 문제와 연결된 Topic은 삭제할 수 없음 |
| TOPIC_004 | 409 | 비활성 Topic |

### DOCUMENT

| 코드 | HTTP | 설명 |
|------|------|------|
| DOCUMENT_001 | 404 | 문서를 찾을 수 없음 |
| DOCUMENT_002 | 409 | 문서 처리가 아직 완료되지 않음 |
| DOCUMENT_003 | 500 | 문서 처리 실패 |
| DOCUMENT_004 | 400 | 지원하지 않는 문서 형식 |

### QUESTION

| 코드 | HTTP | 설명 |
|------|------|------|
| QUESTION_001 | 404 | 개인 문제를 찾을 수 없음 |
| QUESTION_002 | 404 | 공용 문제를 찾을 수 없음 |
| QUESTION_003 | 422 | 시험에 사용할 개인 문제가 부족함 |
| QUESTION_004 | 422 | 시험에 사용할 공용 문제가 부족함 |
| QUESTION_005 | 400 | 문제 유형에 맞는 선택지 수가 아님 |
| QUESTION_006 | 409 | 비활성 공용 문제 |

### EXAM

| 코드 | HTTP | 설명 |
|------|------|------|
| EXAM_001 | 404 | 시험을 찾을 수 없음 |
| EXAM_002 | 409 | 이미 시작된 시험 |
| EXAM_003 | 409 | 진행 중인 시험이 아님 |
| EXAM_004 | 410 | 시험 시간 만료 |
| EXAM_005 | 409 | 이미 제출된 시험 |
| EXAM_006 | 409 | 이미 진행 중인 시험이 있음 |
| EXAM_007 | 404 | 시험 문항을 찾을 수 없음 |
| EXAM_008 | 500 | 시험 문항 참조 정보 오류 |
| EXAM_009 | 409 | 시험 결과를 아직 조회할 수 없음 |

### MONITORING

| 코드 | HTTP | 설명 |
|------|------|------|
| MONITORING_001 | 422 | 지원하지 않는 기간 단위 |
| MONITORING_002 | 422 | 잘못된 조회 기간 범위 |

---

## 1. 인증 API

### 1.1 회원가입

**POST** `/api/auth/v1/register`

```json
{
  "username": "howard",
  "password": "Password1234!",
  "countryCode": "KR"
}
```

| 필드 | 설명 |
|------|------|
| username | 로그인 아이디 |
| password | 비밀번호 |
| countryCode | 문제 생성 언어 결정을 위한 국가 코드 |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "userId": 1,
    "username": "howard"
  }
}
```

### 1.2 로그인

**POST** `/api/auth/v1/login`

```json
{
  "username": "howard",
  "password": "Password1234!"
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "expiresIn": 1800,
    "role": "USER"
  }
}
```

### 1.3 토큰 재발급

**POST** `/api/auth/v1/reissue`

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### 1.4 로그아웃

**POST** `/api/auth/v1/logout`
🔒 인증 필요

### 1.5 비밀번호 재설정

**POST** `/api/auth/v1/reset-password`

아이디 존재 여부를 먼저 확인한 뒤, 확인된 경우에만 새 비밀번호를 입력해 재설정합니다.

```json
{
  "username": "howard",
  "newPassword": "NewPassword1234!"
}
```

---

## 2. Topic API

### 2.1 공용 Topic 목록 조회

**GET** `/topics`
🔒 인증 필요

공용 문제 기반 시험 시작 시 사용할 Topic 목록을 조회합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "topicId": 1,
      "name": "SPRING",
      "description": "Spring Framework and Spring Boot",
      "active": true
    }
  ]
}
```

---

## 3. 사용자 문서 API

### 3.1 문서 업로드 등록

**POST** `/my/documents/upload`
🔒 인증 필요
`multipart/form-data`

| 필드 | 타입 | 설명 |
|------|------|------|
| title | text | 문서 제목 |
| file | file | PDF 또는 MD 파일 |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "documentId": 11,
    "status": "PROCESSING"
  }
}
```

### 3.2 내 문서 목록 조회

**GET** `/my/documents`
🔒 인증 필요

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "documentId": 11,
      "title": "Spring Notes.pdf",
      "status": "PROCESSING",
      "generatedQuestionCount": 0,
      "createdAt": "2026-05-06T10:00:00+09:00",
      "updatedAt": "2026-05-06T10:00:00+09:00"
    }
  ]
}
```

### 3.3 내 문서 상태 조회

**GET** `/my/documents/{documentId}/status`
🔒 인증 필요

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "documentId": 11,
    "title": "Spring Notes.pdf",
    "status": "COMPLETED",
    "generatedQuestionCount": 12
  }
}
```

### 3.4 내 문서 상태 SSE 구독

**GET** `/my/documents/{documentId}/events?accessToken={accessToken}`

브라우저 `EventSource`는 `Authorization` 헤더를 직접 붙일 수 없어서, SSE 구독 시에는 `accessToken`을 쿼리 파라미터로 전달합니다.

**Event Name**

`document-status`

**Event Data**

```json
{
  "documentId": 11,
  "title": "Spring Notes.pdf",
  "status": "COMPLETED",
  "generatedQuestionCount": 12,
  "createdAt": "2026-05-06T10:00:00+09:00",
  "updatedAt": "2026-05-06T10:01:20+09:00"
}
```

---

## 5. 시험 API

### 5.1 시험 생성

**POST** `/exams`
🔒 인증 필요

사용자는 두 가지 방식으로 시험을 생성할 수 있습니다.
- `PUBLIC_TOPIC`: 특정 Topic의 공용 문제
- `PRIVATE_DOCUMENT`: 특정 문서에서 생성된 개인 문제

```json
{
  "sourceType": "PUBLIC_TOPIC",
  "topicId": 1,
  "questionCount": 10,
  "timeLimitMinutes": 30
}
```

또는

```json
{
  "sourceType": "PRIVATE_DOCUMENT",
  "documentId": 11,
  "questionCount": 12,
  "timeLimitMinutes": 20
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "sourceType": "PUBLIC_TOPIC",
    "totalQuestions": 10,
    "timeLimit": 30,
    "status": "CREATED"
  }
}
```

시험 생성 단계에서는 아직 시험이 시작되지 않습니다.

### 5.2 시험 시작

**POST** `/exams/{examId}/start`
🔒 인증 필요

생성된 시험을 실제로 시작합니다. 이 시점에 `startedAt`, `expiredAt`이 확정됩니다.

동일 사용자는 동시에 하나의 `IN_PROGRESS` 시험만 가질 수 있습니다. 이미 진행중인 시험이 있으면 새 시험 시작은 거부되어야 합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "title": "Spring - 시험",
    "sourceType": "PUBLIC_TOPIC",
    "totalQuestions": 10,
    "timeLimitMinutes": 30,
    "startedAt": "2026-04-27T10:00:00+09:00",
    "expiredAt": "2026-04-27T10:30:00+09:00",
    "status": "IN_PROGRESS",
    "remainingSeconds": 1800
  }
}
```

### 5.3 시험 목록 조회

**GET** `/exams`
🔒 인증 필요

현재 로그인 사용자의 `CREATED`, `IN_PROGRESS` 시험 목록을 조회합니다. 시험 홈 화면에서 생성된 시험과 진행중 시험을 함께 표시할 때 사용합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "examId": 102,
      "title": "운영체제 정리.md - 시험",
      "sourceType": "PRIVATE_DOCUMENT",
      "totalQuestions": 12,
      "timeLimitMinutes": 20,
      "status": "CREATED",
      "createdAt": "2026-04-27T09:58:00+09:00",
      "startedAt": null,
      "expiredAt": null,
      "remainingSeconds": 0
    },
    {
      "examId": 101,
      "title": "Spring - 시험",
      "sourceType": "PUBLIC_TOPIC",
      "totalQuestions": 10,
      "timeLimitMinutes": 30,
      "status": "IN_PROGRESS",
      "createdAt": "2026-04-27T09:50:00+09:00",
      "startedAt": "2026-04-27T10:00:00+09:00",
      "expiredAt": "2026-04-27T10:30:00+09:00",
      "remainingSeconds": 1240
    }
  ]
}
```

생성되거나 진행중인 시험이 없으면 `data`는 빈 배열입니다.

### 5.4 시험 문제 조회

**GET** `/exams/{examId}`
🔒 인증 필요

문제 유형별 응답 규칙:
- `MULTIPLE_CHOICE`: 5개 선택지
- `TRUE_FALSE`: 2개 선택지
- `SHORT_ANSWER`: 선택지 없음, 정확 일치형 단답 문제

시험 문제 조회는 시작된 시험(`IN_PROGRESS`) 기준입니다.

### 5.5 답안 저장

**PUT** `/exams/{examId}/answers`
🔒 인증 필요

```json
[
  {
    "questionId": 1,
    "answer": "1"
  },
  {
    "questionId": 2,
    "answer": "TRUE"
  },
  {
    "questionId": 3,
    "answer": "BeanFactory"
  }
]
```

답안 형식:
- 객관식: 선택지 번호 또는 정의된 선택값
- 참/거짓: 2지선다 선택값
- 단답형: 문서에 명시된 정답 문자열과 정확히 일치하는 값

### 5.6 시험 제출

**POST** `/exams/{examId}/submit`
🔒 인증 필요

### 5.7 시험 결과 조회

**GET** `/exams/{examId}/result`
🔒 인증 필요

### 5.8 시험 히스토리 조회

**GET** `/exams/history`
🔒 인증 필요

제출 완료된 시험만 반환합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "examId": 101,
      "title": "Spring - 시험",
      "sourceType": "PUBLIC_TOPIC",
      "totalQuestions": 10,
      "timeLimitMinutes": 30,
      "submittedAt": "2026-04-27T10:31:00+09:00",
      "correctCount": 7,
      "score": 70,
      "pass": true
    },
    {
      "examId": 102,
      "title": "운영체제 정리.md - 시험",
      "sourceType": "PRIVATE_DOCUMENT",
      "totalQuestions": 12,
      "timeLimitMinutes": 20,
      "submittedAt": "2026-04-26T22:14:00+09:00",
      "correctCount": 5,
      "score": 41,
      "pass": false
    }
  ]
}
```

시험 히스토리 상세는 별도 경로를 두지 않고 `GET /exams/{examId}/result`를 재사용합니다.

---

## 6. Admin API

모든 Admin API는 `ADMIN` 권한이 필요합니다.

### 6.1 관리자 로그인

**POST** `/admin/auth/v1/login`

관리자 웹 로그인 전용 API입니다. 일반 사용자 계정은 로그인에 성공하더라도 `AUTH_004` 또는 `AUTH_007`로 차단할 수 있습니다.

```json
{
  "username": "admin-master",
  "password": "Password1234!"
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "adminId": 1,
    "username": "admin-master",
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "expiresIn": 1800,
    "role": "ADMIN"
  }
}
```

### 6.2 관리자 토큰 재발급

**POST** `/admin/auth/v1/reissue`

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "adminId": 1,
    "username": "admin-master",
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "expiresIn": 1800,
    "role": "ADMIN"
  }
}
```

### 6.3 관리자 로그아웃

**POST** `/admin/auth/v1/logout`
🔒 ADMIN

현재 access token을 블랙리스트 처리하고 refresh token을 삭제합니다.

### 6.4 관리자 목록 조회

**GET** `/admin/v1/users`
🔒 ADMIN

관리자 관리 페이지의 목록 표에서 사용합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "adminId": 101,
      "username": "admin-master",
      "active": true,
      "createdAt": "2026-04-27T09:00:00+09:00"
    }
  ]
}
```

### 6.3 관리자 계정 생성

**POST** `/admin/v1/users`
🔒 ADMIN

```json
{
  "username": "admin2",
  "password": "Password1234!"
}
```

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "adminId": 102,
    "username": "admin2",
    "active": true,
    "createdAt": "2026-04-27T10:00:00+09:00"
  }
}
```

### 6.4 관리자 계정 상태 변경

**PATCH** `/admin/v1/users/{userId}/status`
🔒 ADMIN

```json
{
  "active": false
}
```

### 6.5 관리자 계정 삭제

**DELETE** `/admin/v1/users/{userId}`
🔒 ADMIN

### 6.6 관리자 Topic 목록 조회

**GET** `/admin/v1/topics`
🔒 ADMIN

공용문제 관리 > Topic 관리 탭과 문제 등록 다이얼로그의 Topic 선택 목록에서 사용합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "topicId": 1,
      "name": "Java",
      "active": true,
      "questionCount": 12,
      "createdAt": "2026-04-27T09:00:00+09:00"
    }
  ]
}
```

### 6.7 Topic 생성

**POST** `/admin/v1/topics`
🔒 ADMIN

```json
{
  "name": "SPRING"
}
```

### 6.8 Topic 삭제

**DELETE** `/admin/v1/topics/{topicId}`
🔒 ADMIN

Topic 삭제 시 연결된 공용 문제 처리 정책은 별도 비즈니스 규칙으로 정의합니다.

### 6.9 공용 문제 목록 조회

**GET** `/admin/v1/questions`
🔒 ADMIN

공용문제 관리 > 문제 관리 탭의 목록 표에서 사용합니다.

**Query**

| 파라미터 | 설명 |
|----------|------|
| topicId | Topic 기준 필터 |
| difficulty | 난이도 필터 |
| type | 문제 유형 필터 |
| active | 활성/비활성 필터 |

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "questionId": 1001,
      "content": "Spring Boot의 자동 설정 기능을 담당하는 핵심 개념은 무엇인가?",
      "type": "SHORT_ANSWER",
      "difficulty": "EASY",
      "topicId": 1,
      "topicName": "Spring",
      "active": true,
      "createdAt": "2026-04-27T09:00:00+09:00"
    }
  ]
}
```

### 6.10 공용 문제 상세 조회

**GET** `/admin/v1/questions/{questionId}`
🔒 ADMIN

공용문제 관리 페이지에서 행 클릭 시 열리는 상세 다이얼로그에서 사용합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "questionId": 1001,
    "content": "Spring Boot의 자동 설정 기능을 담당하는 핵심 개념은 무엇인가?",
    "type": "SHORT_ANSWER",
    "difficulty": "EASY",
    "topicId": 1,
    "topicName": "Spring",
    "answer": "Auto Configuration",
    "explanation": "Spring Boot는 Auto Configuration을 통해 설정을 자동 적용한다.",
    "options": [],
    "active": true,
    "createdAt": "2026-04-27T09:00:00+09:00"
  }
}
```

### 6.11 공용 문제 등록

**POST** `/admin/v1/questions`
🔒 ADMIN

```json
{
  "topicId": 1,
  "content": "Spring Boot의 자동 설정 기능을 담당하는 핵심 개념은 무엇인가?",
  "type": "SHORT_ANSWER",
  "difficulty": "EASY",
  "answer": "Auto Configuration",
  "explanation": "Spring Boot는 Auto Configuration을 통해 설정을 자동 적용한다.",
  "options": []
}
```

참고:
- `MULTIPLE_CHOICE`는 선택지 5개 필수
- `TRUE_FALSE`는 선택지 2개 필수
- `SHORT_ANSWER`는 서술형 금지, 문서/기준 텍스트에 근거한 명확한 단일 정답만 허용

### 6.12 공용 문제 상태 변경

**PATCH** `/admin/v1/questions/{questionId}/status`
🔒 ADMIN

```json
{
  "active": false
}
```

### 6.13 공용 문제 삭제

**DELETE** `/admin/v1/questions/{questionId}`
🔒 ADMIN

### 6.14 공용 문제 일괄 상태 변경

**PATCH** `/admin/v1/questions/status`
🔒 ADMIN

```json
{
  "questionIds": [1001, 1002, 1003],
  "active": false
}
```

### 6.15 공용 문제 일괄 삭제

**DELETE** `/admin/v1/questions`
🔒 ADMIN

```json
{
  "questionIds": [1001, 1002, 1003]
}
```

### 6.16 사용자 웹 접근 시도 통계 조회

**GET** `/admin/v1/monitoring/access-attempts`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| periodType | `DAILY`, `WEEKLY`, `MONTHLY` |
| from | 조회 시작일 |
| to | 조회 종료일 |

### 6.17 시험 진행 통계 조회

**GET** `/admin/v1/monitoring/exam-runs`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| periodType | `DAILY`, `WEEKLY`, `MONTHLY` |
| from | 조회 시작일 |
| to | 조회 종료일 |

### 6.18 사용자 문서 등록 통계 조회

**GET** `/admin/v1/monitoring/document-registrations`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| periodType | `DAILY`, `WEEKLY`, `MONTHLY` |
| from | 조회 시작일 |
| to | 조회 종료일 |

### 6.19 사용자 문제 생성 통계 조회

**GET** `/admin/v1/monitoring/question-generations`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| periodType | `DAILY`, `WEEKLY`, `MONTHLY` |
| from | 조회 시작일 |
| to | 조회 종료일 |

모니터링 통계 응답 예시:

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "summary": {
      "totalCount": 1284
    },
    "series": [
      {
        "label": "2026-04-21",
        "count": 120
      },
      {
        "label": "2026-04-22",
        "count": 156
      }
    ]
  }
}
```

### 6.20 랜딩 페이지 누적 통계 조회

**GET** `/landing/stats`

로그인 전 랜딩 페이지에서 서비스 누적 이용 현황을 보여주기 위한 공개 API입니다.

#### Response
```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "userPageAccessAttemptCount": 1240,
    "examRunCount": 318,
    "documentRegistrationCount": 205,
    "generatedPrivateQuestionCount": 4821
  }
}
```

---

## 주요 플로우

### 1. 사용자 개인화 문제 생성 플로우

```text
회원가입(username, password, countryCode)
→ 로그인
→ 문서 등록(PDF 또는 MD 업로드)
→ 텍스트 추출/청킹/임베딩
→ countryCode 기반 언어로 문제 생성
→ 문제 저장
→ 원본 문서 삭제
```

### 2. 공용 문제 시험 플로우

```text
ADMIN이 Topic 생성
→ ADMIN이 공용 문제 등록
→ 사용자가 Topic 목록 조회
→ Topic 선택 후 문제 수/시험 시간 입력
→ 시험 생성
→ 시작하기
→ 응시
```

### 3. 개인 문서 시험 플로우

```text
사용자가 개인 문서 등록
→ 개인 문제 생성 완료 확인
→ 문서 선택 후 문제 수/시험 시간 입력
→ 시험 생성
→ 시작하기
→ 응시
```
