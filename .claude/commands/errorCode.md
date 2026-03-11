ErrorCode enum을 읽어 `docs/에러코드.md` 문서를 생성하거나 최신 상태로 동기화합니다.

## 1. ErrorCode 파일 읽기

아래 파일을 읽으세요:
```
tmk-core/src/main/java/com/tmk/core/exception/ErrorCode.java
```

## 2. 문서 생성/업데이트 규칙

`docs/에러코드.md` 파일이 존재하면 업데이트, 없으면 새로 생성하세요.

### 문서 구조

```markdown
# TMK 에러코드 정의

> 최종 업데이트: <오늘 날짜>
> 기준 파일: `tmk-core/src/main/java/com/tmk/core/exception/ErrorCode.java`

---

## 개요

모든 API 에러 응답은 아래 형식을 따릅니다.

\`\`\`json
{
  "errorCode": "AUTH_001",
  "msg": "유효하지 않은 토큰입니다.",
  "data": null
}
\`\`\`

---

## 에러코드 목록

### COMMON (공통)

| 상수명 | errorCode | HTTP Status | 메시지 |
|--------|-----------|-------------|--------|
| ... | ... | ... | ... |

### AUTH (인증/인가)

| 상수명 | errorCode | HTTP Status | 메시지 |
|--------|-----------|-------------|--------|
| ... | ... | ... | ... |

### DOCUMENT (문서)

...

### QUESTION (문제)

...

### EXAM (시험)

...

---

## 사용 방법

### 서비스에서 예외 던지기

\`\`\`java
throw new BusinessException(ErrorCode.EXAM_NOT_FOUND);
\`\`\`

### 컨트롤러 응답

\`\`\`java
return ApiResponse.fail(ErrorCode.DUPLICATE_EMAIL);
\`\`\`

### 응답 예시

\`\`\`json
{
  "errorCode": "EXAM_001",
  "msg": "시험을 찾을 수 없습니다.",
  "data": null
}
\`\`\`
```

## 3. 각 에러코드 항목 추출 기준

`ErrorCode.java`에서 각 enum 상수의 다음 필드를 추출하세요:
- 상수명 (예: `EXAM_NOT_FOUND`)
- 첫 번째 인자 = errorCode 문자열 (예: `"EXAM_001"`)
- 세 번째 인자 = HTTP 상태 코드 (예: `404`)
- 두 번째 인자 = 메시지 (예: `"시험을 찾을 수 없습니다."`)

주석(`// COMMON`, `// AUTH` 등)을 기준으로 그룹을 나누세요.

## 4. 완료 후

생성/업데이트된 파일 경로와 변경된 에러코드 항목을 요약해서 보고하세요.
