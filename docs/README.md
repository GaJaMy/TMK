# TMK 프로젝트 문서 인덱스

| 문서 | 내용 |
|------|------|
| [TMK(Test My Knowledge).md](./TMK(Test My Knowledge).md) | 프로젝트 개요, 요구사항, 구현 현황 |
| [기술 스택.md](./기술 스택.md) | 기술 선택 이유, 개발 환경 설정, 환경 변수 |
| [도메인 모델 설계.md](./도메인 모델 설계.md) | 클린 아키텍처, 포트-어댑터 경계, 도메인 모델 |
| [API 명세서.md](./API 명세서.md) | REST API 전체 명세, 에러 코드 |
| [ERD 설계.md](./ERD 설계.md) | 테이블 구조, 인덱스 설계 |
| [ddl.sql](./ddl.sql) | 실제 DDL SQL |
| [배포 가이드.md](./배포 가이드.md) | GitHub Actions, Docker Hub, Vultr 기반 staging/prod 배포 절차 |

현재 구현 기준 모듈 경계는 `tmk-core`(서비스 + 포트), `tmk-infra`(JPA 어댑터), `tmk-api`(REST/보안), `tmk-batch`(배치)입니다.

## 현재 실행 흐름

- 개발용 인프라 실행: `docker compose up -d` 또는 `docker compose -f docker-compose.dev.yml up -d`
- API 서버 실행: `SPRING_PROFILES_ACTIVE=dev ./gradlew :tmk-api:bootRun`
- 배치 서버 실행: `SPRING_PROFILES_ACTIVE=dev ./gradlew :tmk-batch:bootRun`
- 웹 UI 진입점: `http://localhost:8080/index.html`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

환경 설정은 `application.yml` + `application-{profile}.yml` 구조로 분리되어 있으며, 기본 프로필은 `dev`입니다.

- `dev`: 개인 로컬 개발용. Docker Compose는 PostgreSQL, Redis만 실행하고 API/BATCH는 IntelliJ 또는 `bootRun`으로 실행
- `staging`: 컨테이너 기반 검증 환경. `docker-compose.staging.yml` 사용
- `prod`: 운영 환경. `docker-compose.prod.yml` 사용

민감 정보는 코드에 두지 않고 환경 변수로 주입합니다. 로컬 개발은 직접 환경 변수를 지정하고, staging/prod 는 GitHub Actions가 개별 GitHub Secrets를 조합해 배포 시점에 `.env` 파일을 생성합니다.

정적 프론트는 `tmk-api/src/main/resources/static` 아래에 있으며 현재 기준 페이지 구성은 다음과 같습니다.

- `index.html`: 비로그인 시 로그인/회원가입 진입 화면, 로그인 후 제품 홈
- `exams.html`: 실제 시험 응시 형태의 문제 풀이 화면
- `documents.html`: 내부 문서 등록 및 처리 상태 확인
- `questions.html`: 문제 목록/상세 탐색
- `auth.html`: 인증 센터 및 토큰 재발급 보조 화면

문서의 API 경로와 화면 설명은 현재 코드 구현(`AuthController`, `ExamController`, `DocumentController`, 정적 리소스) 기준으로 유지합니다.
