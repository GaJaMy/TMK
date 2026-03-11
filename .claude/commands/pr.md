현재 브랜치의 변경사항을 바탕으로 GitHub Pull Request를 생성합니다.

## 필수 규칙
- **PR 대상 브랜치는 반드시 `dev`** 입니다. 예외 없이 준수하세요.
- base 브랜치를 `main`으로 설정하는 실수를 하지 마세요.

## 1. 현재 상태 파악

다음 명령어를 병렬로 실행하세요:
- `git status` — 미커밋 변경사항 확인
- `git branch --show-current` — 현재 브랜치 확인
- `git log dev..HEAD --oneline` — dev 브랜치 이후 커밋 목록 확인
- `git diff dev...HEAD --stat` — 변경된 파일 요약

## 2. 사전 확인

- 미커밋 변경사항이 있으면 먼저 `/commit`을 실행하도록 안내하세요.
- 현재 브랜치가 `dev`이면 PR을 생성할 수 없음을 안내하세요.
- 원격 브랜치에 push되지 않은 경우 `git push -u origin <브랜치명>`을 실행하세요.

## 3. PR 내용 작성 규칙

### 제목 형식
```
[<타입>] <변경 요약> (50자 이하)
```

타입: `feat` / `fix` / `refactor` / `chore` / `docs` / `test`

예시:
```
[feat] JWT 인증 필터 및 보안 예외 처리 추가
[fix] 토큰 subject 필드 email로 수정
[refactor] 공통 에러 처리 구조 개선
```

### PR 본문 구조

```markdown
## 변경 사항 요약
- (변경사항을 bullet point로 간결하게)

## 변경 이유
- (왜 이 변경이 필요했는지)

## 주요 변경 파일
- `경로/파일명`: 변경 내용 요약

## 테스트 체크리스트
- [ ] 빌드 성공 확인
- [ ] 주요 기능 동작 확인
- (추가 테스트 항목)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## 4. PR 생성

```bash
gh pr create \
  --base dev \
  --title "[<타입>] <제목>" \
  --body "$(cat <<'EOF'
## 변경 사항 요약
...

## 변경 이유
...

## 주요 변경 파일
...

## 테스트 체크리스트
- [ ] 빌드 성공 확인
- [ ] 주요 기능 동작 확인

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

## 5. 완료 후

생성된 PR URL을 보고하세요.
