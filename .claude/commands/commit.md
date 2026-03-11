변경된 파일을 분석하여 컨벤션에 맞는 Git 커밋을 생성합니다.

## 1. 현재 상태 파악

다음 명령어를 병렬로 실행하세요:
- `git status` — 변경 파일 목록 확인
- `git diff HEAD` — staged/unstaged 변경 내용 전체 확인
- `git log --oneline -5` — 최근 커밋 스타일 참고

## 2. Commit Message 네이밍 규칙 (엄격히 준수)

### 타입 정의

| 타입 | 사용 조건 |
|------|-----------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `chore` | 빌드 설정, 의존성, 설정 파일 변경 |
| `docs` | 문서만 변경 |
| `test` | 테스트 코드 추가/수정 |
| `style` | 포맷팅, 세미콜론 등 코드 동작에 무관한 변경 |
| `perf` | 성능 개선 |
| `security` | 보안 취약점 수정 |

### 형식 규칙

```
<타입>(<스코프>): <제목>

<본문> (선택, 변경 이유 또는 주요 내용)
```

- **제목**: 50자 이하, 현재형 동사 시작, 마침표 없음
- **스코프**: 변경된 모듈/도메인 (예: `auth`, `exam`, `security`, `core`)
- **본문**: 여러 변경사항이 있거나 이유 설명이 필요한 경우에만 작성
- **영어로 작성**

### 예시

```
feat(auth): add JWT token expiry error handling
fix(exam): correct exam status check on answer save
refactor(security): extract SecurityResponseWriter utility
chore(deps): add spring-boot-starter-validation dependency
docs(api): update error codes to match ErrorCode enum
```

## 3. 파일 스테이징 및 커밋

- 관련 없는 파일은 함께 커밋하지 마세요
- `.env`, 인증 정보 파일은 절대 포함하지 마세요
- 아래 형식으로 커밋하세요:

```bash
git add <관련 파일들>
git commit -m "$(cat <<'EOF'
<타입>(<스코프>): <제목>

<본문 (필요한 경우)>

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
```

## 4. 완료 후

`git status`로 커밋이 정상 생성됐는지 확인하고 결과를 보고하세요.
