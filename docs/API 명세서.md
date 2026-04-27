# TMK (Test My Knowledge) API 명세서

> 작성일: 2026-04-27
> 버전: v2.0.0
> Base URL: `/api/v1`

---

이 문서의 관리자 API는 별도 서버가 아니라 `tmk-api` 단일 서버에 포함된 `tmk-admin` 모듈을 통해 `/admin/v1/**` 경로로 제공되는 구조를 전제로 합니다.

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

### TOPIC

| 코드 | HTTP | 설명 |
|------|------|------|
| TOPIC_001 | 404 | Topic을 찾을 수 없음 |
| TOPIC_002 | 409 | 이미 존재하는 Topic 이름 |

### DOCUMENT

| 코드 | HTTP | 설명 |
|------|------|------|
| DOCUMENT_001 | 404 | 문서를 찾을 수 없음 |
| DOCUMENT_002 | 400 | PDF 파일만 업로드 가능 |
| DOCUMENT_003 | 422 | 읽을 수 없는 노션/URL |

### QUESTION

| 코드 | HTTP | 설명 |
|------|------|------|
| QUESTION_001 | 404 | 문제를 찾을 수 없음 |
| QUESTION_002 | 422 | 시험을 생성할 문제가 부족함 |

### EXAM

| 코드 | HTTP | 설명 |
|------|------|------|
| EXAM_001 | 404 | 시험을 찾을 수 없음 |
| EXAM_002 | 409 | 이미 제출된 시험 |
| EXAM_003 | 410 | 시험 시간 만료 |
| EXAM_004 | 400 | 잘못된 시험 생성 조건 |
| EXAM_005 | 409 | 이미 시작된 시험 |

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

### 4.2 공용 문제 조회

**GET** `/questions/public`
🔒 인증 필요

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

### 6.1 admin 계정 생성

**POST** `/admin/users`
🔒 ADMIN

```json
{
  "username": "admin2",
  "password": "Password1234!",
  "countryCode": "KR"
}
```

### 6.2 Topic 생성

**POST** `/admin/topics`
🔒 ADMIN

```json
{
  "name": "SPRING",
  "description": "Spring ecosystem"
}
```

### 6.3 Topic 수정/활성화 관리

**PATCH** `/admin/topics/{topicId}`
🔒 ADMIN

```json
{
  "name": "SPRING BOOT",
  "description": "Spring Boot focused topic",
  "active": true
}
```

### 6.4 Topic 목록 조회

**GET** `/admin/topics`
🔒 ADMIN

### 6.5 공용 문제 등록

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

### 6.6 공용 문제 목록 조회

**GET** `/admin/questions`
🔒 ADMIN

**Query**

| 파라미터 | 설명 |
|----------|------|
| topicId | Topic 기준 필터 |
| difficulty | 난이도 필터 |
| type | 문제 유형 필터 |

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
