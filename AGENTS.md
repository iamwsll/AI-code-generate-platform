# Repository Guidelines

## Project Structure & Module Organization
- Backend Spring Boot code lives in `src/main/java` with configs/templates in `src/main/resources`; integration fixtures and schemas under `sql/` and docs in `docs/`.
- Frontend Vue 3 app is in `ai-code-frontend/`; static assets stay in `ai-code-frontend/src/assets` and composables/stores in `ai-code-frontend/src`.
- Generated code and deployment artifacts are kept out of VCS in `tmp/code_output` and `tmp/code_deploy`—avoid manual edits there.
- Tests reside in `src/test/java` (JUnit/Spring Boot) and any future front-end tests should mirror source layout inside `ai-code-frontend/src`.

## Build, Test, and Development Commands
- Backend dev server: `./mvnw spring-boot:run` (reads `application-local.yml` for overrides).
- Backend build & unit tests: `./mvnw clean verify` (fails on test or format issues).
- Frontend dev: `cd ai-code-frontend && npm install && npm run dev` (Vite dev server, default port 5173).
- Frontend build: `cd ai-code-frontend && npm run build` (emits to `dist/`).
- Lint/format front-end: `cd ai-code-frontend && npm run lint` and `npm run format`.

## Coding Style & Naming Conventions
- Java: follow Spring idioms; prefer constructor injection; classes in `dev/…` package use `UpperCamelCase`, beans `@Component`/`@Service` names stay default; keep 4-space indent.
- Vue/TS: `script setup` + components in `PascalCase.vue`; composables `useXxx.ts`; stores under `stores/`; keep type-first (`TypeName`) interfaces.
- Formatting: Prettier + ESLint configured for the frontend; rely on IntelliJ/IDEA code style defaults for Java; avoid trailing whitespace and keep methods short.

## Testing Guidelines
- Backend tests use JUnit 5 via `spring-boot-starter-test`; place new tests alongside package paths under `src/test/java` and name classes `*Test`.
- Mock external services; keep integration tests using `@SpringBootTest` thin and isolated; prefer `@DataJpaTest` for repository coverage.
- Frontend: no default harness—add Vitest or Cypress only as needed; colocate tests near components and gate them with `npm run test` if introduced.

## Commit & Pull Request Guidelines
- Commit messages in history use terse changelog-style titles (e.g., `0.2.1 - 修复了模型timeout的问题`); follow either semantic (`fix: …`) or version-note style but stay imperative and scoped.
- One logical change per commit; ensure builds pass locally before pushing.
- PRs should include: purpose summary, linked issue/Story ID, setup notes, screenshots or API samples for UI/API changes, and checklist of verification commands run.
- Keep diffs small; highlight risky areas (caching, concurrency, model tool-calls) in the PR description.

## Security & Configuration Tips
- Do not commit secrets; prefer env files consumed by `application-local.yml` and frontend `.env.local` (ignored by git).
- Redis/MySQL/OSS endpoints must be configurable via env vars; verify credentials are injected at runtime, not hardcoded.
- When touching Selenium or external fetchers, confirm timeouts and headless flags are set to avoid CI hangs.
