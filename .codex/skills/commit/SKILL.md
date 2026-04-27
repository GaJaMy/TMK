---
name: commit
description: Generate Korean git commits and commit messages with a required body based on the current repository changes. Use when the user asks for a commit, asks for a Korean commit title/body, wants commit command usage, or asks to summarize staged or working tree changes into commit-ready text.
---

# Commit Message Generator

Generate a Korean commit message, and when asked, a full git commit command, from the current repository changes.

## When to use

- The user asks for a commit message.
- The user asks to create a commit.
- The user wants the message in Korean.
- The user wants both subject and body.
- The user wants the exact command to run.
- The user wants the message based on current git changes.

## Workflow

1. Inspect the current changes with `git status --short` and `git diff --stat`.
2. Read focused diffs only for files that materially changed.
3. Infer the main intent of the change.
4. Write a Korean commit message with:
   - one subject line
   - a blank line
   - body bullets
5. If the user asks for a commit command, provide:
   - an example `git add` command only for the relevant files when the scope is clear
   - a `git commit` command that includes both subject and body
6. If unrelated pre-existing changes exist, mention that the message targets only the relevant changes.

## Format rules

- The commit message must be in Korean.
- The body is mandatory. Never return a subject-only commit message unless the user explicitly overrides this rule.
- Default to a conventional prefix in repository style:
  - `feat:`
  - `fix:`
  - `refactor:`
  - `docs:`
  - `test:`
  - `chore:`
- Keep the subject to one line.
- The body should explain:
  - what changed
  - why it changed if that is visible from the diff
- Prefer 2-4 bullet lines in the body.
- Do not invent behavior that is not supported by the diff.
- Do not include file-by-file noise unless the user asks.
- Prefer concrete nouns from the codebase over vague phrases.

## Output template

```text
type: 핵심 변경 사항 요약

- 변경 1
- 변경 2
- 필요하면 변경 이유
```

## Command template

When the user asks for the actual command, prefer this shape:

```bash
git add <relevant-files>
git commit -m "type: 한 줄 제목" -m "- 변경 1
- 변경 2
- 변경 이유"
```

If the file list is not clear or there are unrelated changes in the worktree, do not guess broadly. Either:

- provide only the `git commit` command text, or
- clearly say the message is for the relevant changes only

## Command rules

- Use `git commit -m "제목" -m "본문"` so the body is always included.
- Do not suggest a one-line `git commit -m "제목"` when this skill is active.
- Do not stage unrelated files just to make the command look complete.
- If the user asks to split commits, provide separate messages and separate `git add` targets.

## Special handling

- For documentation-only changes, prefer `docs:`.
- For mixed structural changes, choose the prefix by the primary outcome, not the number of files.
- If the change is too broad for one clean commit, say so and provide:
  - one recommended combined message
  - optional split commit suggestions
