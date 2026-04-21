# Repository Guidelines

## Project Structure & Module Organization
This repository is a multi-module Gradle project with four application areas:

- `tmk-core`: domain entities, application services, and outbound port interfaces. Unit tests live in `src/test/java`.
- `tmk-infra`: shared persistence adapters and Spring Data JPA repositories that implement core ports.
- `tmk-api`: Spring Boot API app. Main code lives in `src/main/java`, config in `src/main/resources`, tests in `src/test`. Static web pages live in `src/main/resources/static`.
- `tmk-batch`: Spring Boot batch app for scheduled and job-based processing. Source is under `src/main/java`; resources are under `src/main/resources`.

Root build configuration is in `build.gradle` and included modules are declared in `settings.gradle`.

## Build, Test, and Development Commands
- `./gradlew build`: compile all modules and run tests.
- `./gradlew test`: run the full test suite across modules.
- `./gradlew :tmk-api:bootRun`: start the API application locally.
- `./gradlew :tmk-batch:bootRun`: start the batch application locally.
- `./gradlew :tmk-core:test`: run only core module tests.
- `./gradlew :tmk-infra:classes`: compile shared JPA adapters and repositories.
- `docker compose up -d`: start local development infrastructure only (`postgres`, `redis`).
- `docker compose -f docker-compose.dev.yml up -d`: explicit development compose file.
- `docker compose --env-file .env.staging -f docker-compose.staging.yml pull && docker compose --env-file .env.staging -f docker-compose.staging.yml up -d`: deploy staging from pushed images.
- `docker compose --env-file .env.prod -f docker-compose.prod.yml pull && docker compose --env-file .env.prod -f docker-compose.prod.yml up -d`: deploy production-style stack from pushed images.

For UI checks, open `http://localhost:8080/index.html` after starting `tmk-api`.
Swagger UI is available at `http://localhost:8080/swagger-ui.html` when enabled for the active profile.

Use module-scoped commands when iterating on one area to keep feedback fast.

## Coding Style & Naming Conventions
Use Java 21 and standard Spring Boot conventions. Follow the existing style:

- 4-space indentation, no tabs.
- Packages under `com.tmk.<module>...`.
- Classes in `PascalCase`, methods and fields in `camelCase`, constants in `UPPER_SNAKE_CASE`.
- Prefer constructor injection; the codebase commonly uses Lombok `@RequiredArgsConstructor`.
- Keep controllers, services, ports, and adapters in clearly named packages.
- In `tmk-core`, depend on outbound ports rather than Spring Data repositories directly.
- Group outbound ports by concern: `port.out.persistence`, `port.out.ai`, `port.out.cache`, and `port.out.security`.

No formatter or lint task is configured in Gradle today, so match surrounding code closely.

## Testing Guidelines
Tests use JUnit 5 via `useJUnitPlatform()` and Spring Boot test support. Batch tests also use `spring-batch-test`.

- Place tests under the matching module, for example `tmk-core/src/test/java/...`.
- Name test classes with the `*Test` suffix.
- Add focused unit tests for domain logic in `tmk-core`; use Spring integration tests only when wiring matters.

## Commit & Pull Request Guidelines
Recent history follows short, imperative subjects, often with a type or spec tag, for example:

- `feat(SPEC-BATCH-001): implement batch job scheduling and execution logic`
- `test: add unit tests for auth, document, and question domain services`

Prefer `feat:`, `fix:`, `test:`, or `refactor:` prefixes. PRs should include scope, affected modules, verification steps, and any API or batch behavior changes. Include request/response examples when controller behavior changes.

## Configuration Notes
Do not commit secrets. Profile-specific settings are split across `application-dev.yml`, `application-staging.yml`, and `application-prod.yml`, with shared defaults in each module’s `application.yml`. Use environment variables for sensitive values such as `JWT_SECRET`, `OPENAI_API_KEY`, mail credentials, and database passwords. For staging and production, GitHub Actions assembles `.env` files from individual GitHub Secrets at deploy time instead of storing env files in the repository. Keep JPA repositories in `tmk-infra` and wire them into `tmk-api` and `tmk-batch` through port implementations rather than direct repository injection.

## Deployment Notes
Staging deployment is driven by [`.github/workflows/staging-deploy.yml`](/Users/howard/Desktop/personal/tmk/.github/workflows/staging-deploy.yml). The workflow builds Docker images for the `api` and `batch` targets, pushes them to Docker Hub, uploads compose assets to the server, assembles `.env.staging` from individual GitHub Secrets, and runs Docker Compose remotely on Vultr. Keep compose files image-based for staging and production; do not switch them back to local `build:` blocks.
