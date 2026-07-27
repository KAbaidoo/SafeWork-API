## Overview

This repository is a small Vite + React + TypeScript frontend for the SafeWork product. Keep edits minimal and type-safe. The app entry is `index.html` -> `src/main.tsx` -> `src/App.tsx`.

## Quick commands

- Start dev server (Vite): `npm run dev` (server port set in `vite.config.ts` to 5173)
- Build for production: `npm run build`
- Preview a production build: `npm run preview` (script uses port 4173)
- Type-check only: `npm run typecheck`
- Lint sources: `npm run lint` and autofix with `npm run lint:fix`

Note: Husky hooks are present; run `npm run prepare` once to install them in a fresh clone.

## Architecture & important files

- `index.html` — single-page entry that loads `/src/main.tsx`.
- `src/main.tsx` — React root setup (uses `React.StrictMode`).
- `src/App.tsx` — top-level App component and the best place to wire global layout/routes.
- `src/index.css` — global styles loaded from `main.tsx`.
- `vite.config.ts` — Vite config; imports `@vitejs/plugin-react` dynamically and sets dev server port to 5173.
- `package.json` — central place for scripts and dev dependencies (ESLint, Prettier, Husky, lint-staged, TypeScript, Vite).
- `tsconfig.json` — strict TypeScript settings ("strict": true) and `moduleResolution: "Bundler"`.
- `eslint.config.cjs` — ESLint configuration used by CI/local linting hooks.
- `docs/` — Product PRDs and API structure docs (see `safework_api_structure.md`) — useful for understanding expected backend contracts.

## Conventions and patterns (project-specific)

- Typescript: strict mode is enabled. Always run `npm run typecheck` after structural changes.
- Imports: prefer relative imports for local modules (standard ESM imports are used). Keep file extensions `.tsx` for components.
- Formatting & linting: Prettier + ESLint are configured and enforced via lint-staged on staged files. Committing typically runs `prettier --write` and `eslint --fix` for source files.
- No frontend tests are configured in the repository. If you add tests, include an npm script for them and update README.

## Integration points

- There is no direct API client in the repo yet, but the `docs/safework_api_structure.md` describes backend endpoints. When adding networking:
  - Centralize API calls into a `src/api/` folder
  - Use typed request/response interfaces and export them for components

## Editing guidance for AI agents

- Small, focused PRs: this repo is intentionally small — change one component or concern per PR.
- Run these checks locally before proposing changes:
  1. `npm run typecheck` — ensures TypeScript types are clean
  2. `npm run lint` — enforces style and rule consistency
  3. `npm run dev` — sanity-check UI changes in the browser (port 5173)
- When adding new dependencies, update `package.json` and ensure `vite` and TypeScript compatibility.

## Examples from this codebase

- App entry: `src/main.tsx` creates the root and renders `<App />` (modify here for global providers).
- Vite port: `vite.config.ts` sets server port to `5173`, so prefer that for dev runs; the `preview` script uses `--port 4173` — watch for this mismatch when testing preview builds.

## Files to reference when unsure

- `package.json` — scripts & hooks
- `vite.config.ts` — dev server behaviour
- `tsconfig.json` — compiler expectations
- `eslint.config.cjs` — linting rules
- `docs/safework_api_structure.md` — backend contracts

If anything here is unclear or you need more examples (routing, state management, API client), tell me which area to expand and I'll update this file.

---

## Backend API specifics (summary from `docs/safework_api_structure.md`)

- Backend is a Spring Boot service (base package `com.safework.api`). Expect REST endpoints under versioned paths like `/v1/...` and some analytics endpoints under `/analytics/...`.
- Authentication: JWT-based. Clients must send `Authorization: Bearer <token>` and handle 401s by routing to login/refresh.
- Multi-tenant: many endpoints are organization-scoped. Include organization context (path/query param or token claim) where appropriate.
- Version-based sync: key write operations (inspections, checklists, tasks) include a `version` field.
  - Clients should include their local `version` on updates. Server accepts updates when versions match and returns a conflict (409) when they don't. Provide a UI flow for conflict resolution.
- Dev helpers: `DataSeeder` seeds sample data; `application-dev.yml` exists for local integration.
- OpenAPI/Swagger is planned — prefer it when available for DTO generation.

Common endpoints (doc-derived examples):

- `POST /auth/login` -> returns JWT (`LoginResponse`)
- `GET /v1/users`, `POST /v1/users`, `PUT /v1/users/{id}`
- `GET /v1/assets`, `GET /v1/assets/{id}`, `POST /v1/assets`
- `GET /v1/checklists`, `PUT /v1/checklists/{id}` (updates should increment `version`)
- `POST /v1/inspections` (include checklist `version`), `GET /v1/inspections/{id}`
- `POST /v1/issues`, `GET /v1/issues`
- `GET /analytics/completion-rate`, `GET /analytics/top-issues`, `GET /analytics/asset-history/{id}`

Client integration recommendations for this repo:

- Create `src/api/` and export a small typed HTTP client:
  - Set `Authorization` header automatically from a token store.
  - Parse backend error shapes (see `ErrorResponse` / `GlobalExceptionHandler` in docs) and surface them as typed errors.
  - Provide helper methods for `GET`, `POST`, `PUT`, `DELETE` that accept typed DTOs.
- Add typed DTOs mirroring backend names (e.g. `AssetDto`, `CreateAssetRequest`, `LoginResponse`) — keep them narrow and updated from `docs/` or generated OpenAPI when available.
- Implement conflict handling for 409 responses (offer "refresh", "overwrite", or "merge").
- When adding features, reference `docs/prds/` for product behavior and `/docs/safework_api_structure.md` for precise contract expectations.

If you'd like, I can:

- Add a minimal `src/api/client.ts` and one example typed call (`auth.login`) to this repository.
- Create a small CI workflow that runs `npm run typecheck` and `npm run lint` on PRs.

Please review and tell me if you want any additional project-specific rules (branch naming, commit format, required PR checks) or a sample API client added.
## Overview

This repository is a small Vite + React + TypeScript frontend for the SafeWork product. Keep edits minimal and type-safe. The app entry is `index.html` -> `src/main.tsx` -> `src/App.tsx`.

## Quick commands

- Start dev server (Vite): `npm run dev` (server port set in `vite.config.ts` to 5173)
- Build for production: `npm run build`
- Preview a production build: `npm run preview` (script uses port 4173)
- Type-check only: `npm run typecheck`
- Lint sources: `npm run lint` and autofix with `npm run lint:fix`

Note: Husky hooks are present; run `npm run prepare` once to install them in a fresh clone.

## Architecture & important files

- `index.html` — single-page entry that loads `/src/main.tsx`.
- `src/main.tsx` — React root setup (uses `React.StrictMode`).
- `src/App.tsx` — top-level App component and the best place to wire global layout/routes.
- `src/index.css` — global styles loaded from `main.tsx`.
- `vite.config.ts` — Vite config; imports `@vitejs/plugin-react` dynamically and sets dev server port to 5173.
- `package.json` — central place for scripts and dev dependencies (ESLint, Prettier, Husky, lint-staged, TypeScript, Vite).
- `tsconfig.json` — strict TypeScript settings ("strict": true) and `moduleResolution: "Bundler"`.
- `eslint.config.cjs` — ESLint configuration used by CI/local linting hooks.
- `docs/` — Product PRDs and API structure docs (see `safework_api_structure.md`) — useful for understanding expected backend contracts.

## Conventions and patterns (project-specific)

- Typescript: strict mode is enabled. Always run `npm run typecheck` after structural changes.
- Imports: prefer relative imports for local modules (standard ESM imports are used). Keep file extensions `.tsx` for components.
- Formatting & linting: Prettier + ESLint are configured and enforced via lint-staged on staged files. Committing typically runs `prettier --write` and `eslint --fix` for source files.
- No frontend tests are configured in the repository. If you add tests, include an npm script for them and update README.

## Integration points

- There is no direct API client in the repo yet, but the `docs/safework_api_structure.md` describes backend endpoints. When adding networking:
  - Centralize API calls into a `src/api/` folder
  - Use typed request/response interfaces and export them for components

## Editing guidance for AI agents

- Small, focused PRs: this repo is intentionally small — change one component or concern per PR.
- Run these checks locally before proposing changes:
  1. `npm run typecheck` — ensures TypeScript types are clean
  2. `npm run lint` — enforces style and rule consistency
  3. `npm run dev` — sanity-check UI changes in the browser (port 5173)
- When adding new dependencies, update `package.json` and ensure `vite` and TypeScript compatibility.

## Examples from this codebase

- App entry: `src/main.tsx` creates the root and renders `<App />` (modify here for global providers).
- Vite port: `vite.config.ts` sets server port to `5173`, so prefer that for dev runs; the `preview` script uses `--port 4173` — watch for this mismatch when testing preview builds.

## Files to reference when unsure

- `package.json` — scripts & hooks
- `vite.config.ts` — dev server behaviour
- `tsconfig.json` — compiler expectations
- `eslint.config.cjs` — linting rules
- `docs/safework_api_structure.md` — backend contracts

If anything here is unclear or you need more examples (routing, state management, API client), tell me which area to expand and I'll update this file.
