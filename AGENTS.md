# Repository Guidelines

## Project Structure & Module Organization
Backend code lives under `src/main/java` with configuration and templates in `src/main/resources`; Spring Boot is bootstrapped from `cn.iamwsll.aicode.AiCodeApplication`. Generated artifacts and deployment bundles land in `tmp/code_output` and `tmp/code_deploy`, so keep them gitignored. The Vue client sits in `ai-code-frontend/` (sources in `src/`, routing in `src/router`, stores in `src/stores`). Shared documentation is under `docs/`, while SQL schemas are under `sql/` (notably `create_table.sql`). If you touch monitoring, check `monitor_config/` for Prometheus scrape examples.

## Build, Test, and Development Commands
- `./mvnw clean install` — compiles the Spring Boot app with Java 21 and runs unit tests.
- `./mvnw spring-boot:run` — starts the backend with the active profile from `application-*.yml`.
- `cd ai-code-frontend && npm install` — installs the Vite/Vue toolchain.
- `npm run dev` — launches the frontend dev server (proxy assumes the backend on `localhost:8080`).
- `npm run build` — produces a production bundle in `ai-code-frontend/dist`.
- `npm run lint` / `npm run format` / `npm run type-check` — eslint + prettier + vue-tsc sanity checks before sending PRs.

## Coding Style & Naming Conventions
Java follows the Spring Boot defaults: 4-space indentation, Lombok for boilerplate, `UpperCamelCase` for classes, and `snake_case` for SQL/table artifacts. Keep packages under `cn.iamwsll.aicode.<domain>` and prefer constructor injection. Vue/TS uses ESLint + Prettier (single quotes, semicolons) and `<script setup lang="ts">` components; Pinia stores live in `src/stores` using `useXxxStore` naming. Keep generated resource folders (`tmp/*` and `image/`) write-only and avoid committing them.

## Testing Guidelines
Backend tests rely on Spring Boot Test + JUnit 5 (`./mvnw test`). Co-locate tests under `src/test/java` mirroring the package under test, and name classes `*Tests`. Mock external services (LLM, Redis, OSS) via test configs or containers to keep CI deterministic. Frontend validation currently depends on static checks; run `npm run lint` and `npm run type-check` and include targeted Vitest specs when introducing logic-heavy composables or stores.

## Commit & Pull Request Guidelines
Recent history shows version-tagged summaries (e.g., `0.2.1 - 修复模型 timeout`). Follow that pattern: `[scope or version] - concise change note`, mixing Chinese descriptions when user-facing. Each PR should describe the motivation, list backend/frontend touchpoints, link the relevant issue, and attach screenshots or API traces when UI or contract changes occur. Confirm SQL migrations, config keys, and monitoring dashboards in the PR checklist so reviewers can validate deployments quickly.
