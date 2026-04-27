# TMK (Test My Knowledge) API 명세서

> 작성일: 2026-04-27
> 버전: v2.0.0
> Base URL: `/api/v1`

---

이 문서의 관리자 API는 별도 서버가 아니라 `tmk-api` 단일 서버 내부의 관리자 패키지/도메인을 통해 `/admin/v1/**` 경로로 제공되는 구조를 전제로 합니다.

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
| AUTH_001 | 401 | 유효하지 않은 토큰 |
| AUTH_002 | 401 | 만료된 토큰 |
| AUTH_003 | 401 | 인증 필요 |
| AUTH_004 | 403 | 권한 없음 |
| AUTH_005 | 409 | 이미 사용 중인 아이디 |
| AUTH_006 | 401 | 아이디 또는 비밀번호 불일치 |
| AUTH_007 | 403 | admin만 admin 계정을 생성 가능 |
| AUTH_008 | 401 | 유효하지 않은 리프레시 토큰 |
| AUTH_009 | 403 | 비활성화된 계정 |
| AUTH_010 | 404 | 관리자를 찾을 수 없음 |

### TOPIC

| 코드 | HTTP | 설명 |
|------|------|------|
| TOPIC_001 | 404 | Topic을 찾을 수 없음 |
| TOPIC_002 | 409 | 이미 존재하는 Topic 이름 |
| TOPIC_003 | 409 | 공용 문제와 연결된 Topic은 삭제할 수 없음 |
| TOPIC_004 | 422 | 잘못된 Topic 생성 요청 |

### DOCUMENT

| 코드 | HTTP | 설명 |
|------|------|------|
| DOCUMENT_001 | 404 | 문서를 찾을 수 없음 |
| DOCUMENT_002 | 400 | PDF 파일만 업로드 가능 |
| DOCUMENT_003 | 422 | 읽을 수 없는 노션/URL |
| DOCUMENT_004 | 403 | 다른 사용자의 문서에 접근할 수 없음 |

### QUESTION

| 코드 | HTTP | 설명 |
|------|------|------|
| QUESTION_001 | 404 | 문제를 찾을 수 없음 |
| QUESTION_002 | 422 | 시험을 생성할 문제가 부족함 |
| QUESTION_003 | 422 | 잘못된 문제 생성 요청 |
| QUESTION_004 | 409 | 이미 비활성화된 문제 |
| QUESTION_005 | 409 | 이미 활성화된 문제 |
| QUESTION_006 | 422 | 객관식 선택지 개수 오류 |
| QUESTION_007 | 422 | 참/거짓 선택지 개수 오류 |
| QUESTION_008 | 422 | 단답형 정답 형식 오류 |
| QUESTION_009 | 403 | 다른 사용자의 개인 문제에 접근할 수 없음 |

### EXAM

| 코드 | HTTP | 설명 |
|------|------|------|
| EXAM_001 | 404 | 시험을 찾을 수 없음 |
| EXAM_002 | 409 | 이미 제출된 시험 |
| EXAM_003 | 410 | 시험 시간 만료 |
| EXAM_004 | 400 | 잘못된 시험 생성 조건 |
| EXAM_005 | 409 | 이미 시작된 시험 |
| EXAM_006 | 403 | 다른 사용자의 시험에 접근할 수 없음 |

### MONITORING

| 코드 | HTTP | 설명 |
|------|------|------|
| MONITORING_001 | 422 | 지원하지 않는 기간 단위 |
| MONITORING_002 | 422 | 잘못된 조회 기간 범위 |

---

## 1. 인증 API

### 1.1 회원가입

**POST** `/auth/register`

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
    "username": "howard",
    "role": "USER"
  }
}
```

### 1.2 로그인

**POST** `/auth/login`

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

**POST** `/auth/reissue`

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### 1.4 로그아웃

**POST** `/auth/logout`
🔒 인증 필요

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

### 3.1 PDF 업로드 등록

**POST** `/my/documents/upload`
🔒 인증 필요
`multipart/form-data`

| 필드 | 타입 | 설명 |
|------|------|------|
| title | text | 문서 제목 |
| file | file | PDF 파일 |

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

### 3.2 노션/URL 등록

**POST** `/my/documents/link`
🔒 인증 필요

```json
{
  "title": "Spring Notes",
  "sourceType": "NOTION",
  "sourceReference": "https://www.notion.so/example"
}
```

`sourceType`은 `NOTION`, `URL` 중 하나입니다.

### 3.3 내 문서 목록 조회

**GET** `/my/documents`
🔒 인증 필요

### 3.4 내 문서 상태 조회

**GET** `/my/documents/{documentId}/status`
🔒 인증 필요

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "documentId": 11,
    "title": "Spring Notes",
    "status": "COMPLETED",
    "generatedQuestionCount": 12
  }
}
```

---

## 4. 문제 조회 API

### 4.1 내 문서 기반 문제 조회

**GET** `/my/questions`
🔒 인증 필요

**Query**

| 파라미터 | 설명 |
|----------|------|
| documentId | 특정 문서 기반 문제만 조회 |

### 4.2 공용 문제 목록 조회

**GET** `/questions/public`
🔒 인증 필요

관리자 웹의 `공용문제 관리 > 문제 관리` 탭에서 사용한다.

**Query**

| 파라미터 | 설명 |
|----------|------|
| topicId | Topic 기준 필터 |

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

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": {
    "examId": 101,
    "startedAt": "2026-04-27T10:00:00+09:00",
    "expiredAt": "2026-04-27T10:30:00+09:00",
    "status": "IN_PROGRESS"
  }
}
```

### 5.3 시험 문제 조회

**GET** `/exams/{examId}`
🔒 인증 필요

문제 유형별 응답 규칙:
- `MULTIPLE_CHOICE`: 5개 선택지
- `TRUE_FALSE`: 2개 선택지
- `SHORT_ANSWER`: 선택지 없음, 정확 일치형 단답 문제

시험 문제 조회는 시작된 시험(`IN_PROGRESS`) 기준입니다.

### 5.4 답안 저장

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

### 5.5 시험 제출

**POST** `/exams/{examId}/submit`
🔒 인증 필요

### 5.6 시험 결과 조회

**GET** `/exams/{examId}/result`
🔒 인증 필요

### 5.7 시험 히스토리 조회

**GET** `/exams/history`
🔒 인증 필요

### 5.8 시험 히스토리 상세 조회

**GET** `/exams/history/{examId}`
🔒 인증 필요

---

## 6. Admin API

모든 Admin API는 `ADMIN` 권한이 필요합니다.

### 6.1 관리자 로그인

**POST** `/admin/auth/login`

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
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "expiresIn": 1800,
    "role": "ADMIN"
  }
}
```

### 6.2 관리자 목록 조회

**GET** `/admin/users`
🔒 ADMIN

관리자 관리 페이지의 목록 표에서 사용합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "userId": 101,
      "username": "admin-master",
      "active": true,
      "createdAt": "2026-04-27T09:00:00+09:00"
    }
  ]
}
```

### 6.3 관리자 계정 생성

**POST** `/admin/users`
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
    "userId": 102,
    "username": "admin2",
    "active": true,
    "createdAt": "2026-04-27T10:00:00+09:00"
  }
}
```

### 6.4 관리자 계정 상태 변경

**PATCH** `/admin/users/{userId}/status`
🔒 ADMIN

```json
{
  "active": false
}
```

### 6.5 관리자 계정 삭제

**DELETE** `/admin/users/{userId}`
🔒 ADMIN

### 6.6 관리자 Topic 목록 조회

**GET** `/admin/topics`
🔒 ADMIN

공용문제 관리 > Topic 관리 탭에서 사용합니다.

**Response**

```json
{
  "errorCode": "SUCCESS",
  "msg": "ok",
  "data": [
    {
      "topicId": 1,
      "name": "Java",
      "questionCount": 12,
      "createdAt": "2026-04-27T09:00:00+09:00"
    }
  ]
}
```

### 6.7 Topic 생성

**POST** `/admin/topics`
🔒 ADMIN

```json
{
  "name": "SPRING"
}
```

### 6.8 Topic 삭제

**DELETE** `/admin/topics/{topicId}`
🔒 ADMIN

Topic 삭제 시 연결된 공용 문제 처리 정책은 별도 비즈니스 규칙으로 정의합니다.

### 6.9 공용 문제 목록 조회

**GET** `/admin/questions`
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

**GET** `/admin/questions/{questionId}`
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

**POST** `/admin/questions`
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

**PATCH** `/admin/questions/{questionId}/status`
🔒 ADMIN

```json
{
  "active": false
}
```

### 6.13 공용 문제 삭제

**DELETE** `/admin/questions/{questionId}`
🔒 ADMIN

### 6.14 공용 문제 일괄 상태 변경

**PATCH** `/admin/questions/bulk/status`
🔒 ADMIN

```json
{
  "questionIds": [1001, 1002, 1003],
  "active": false
}
```

### 6.15 공용 문제 일괄 삭제

**DELETE** `/admin/questions/bulk`
🔒 ADMIN

```json
{
  "questionIds": [1001, 1002, 1003]
}
```

### 6.16 사용자 웹 접근 시도 통계 조회

**GET** `/admin/monitoring/access-attempts`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| periodType | `DAILY`, `WEEKLY`, `MONTHLY` |
| from | 조회 시작일 |
| to | 조회 종료일 |

### 6.17 시험 진행 통계 조회

**GET** `/admin/monitoring/exam-runs`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| periodType | `DAILY`, `WEEKLY`, `MONTHLY` |
| from | 조회 시작일 |
| to | 조회 종료일 |

### 6.18 사용자 문서 등록 통계 조회

**GET** `/admin/monitoring/document-registrations`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| periodType | `DAILY`, `WEEKLY`, `MONTHLY` |
| from | 조회 시작일 |
| to | 조회 종료일 |

### 6.19 사용자 문제 생성 통계 조회

**GET** `/admin/monitoring/question-generations`
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

---

## 주요 플로우

### 1. 사용자 개인화 문제 생성 플로우

```text
회원가입(username, password, countryCode)
→ 로그인
→ 문서 등록(PDF 또는 읽기 가능한 NOTION/URL)
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
