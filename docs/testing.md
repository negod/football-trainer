# Testing

## Backend

Run:

```bash
npm run test:backend
```

Expected test types:

- Domain tests for business rules.
- Use-case tests with mocked ports.
- Controller tests for validation, authorization, and status codes.
- Persistence tests for JPA mapping and Liquibase schema behavior when the feature depends on PostgreSQL.

## Frontend

Run:

```bash
npm run test:frontend
```

Expected test types:

- Component behavior with React Testing Library.
- API client error handling.
- Page-level workflows for user-facing flows.

## End-to-end (Playwright)

Specs live in `frontend/e2e`, configured by `frontend/playwright.config.ts`.
They exercise the running app over HTTP, so Postgres, the backend and the
frontend must already be up. `mission-control` (see
[`mission-control/README.md`](../mission-control/README.md)) starts whatever
is missing, waits for each service's health check, then runs the suite:

```bash
npm run mc:e2e
```

Or, with the stack already running:

```bash
npm run test:e2e
```

One-time setup, after `npm install --prefix frontend`:

```bash
npx --prefix frontend playwright install --with-deps chromium
```

Expected coverage: the backend health endpoint, the app shell rendering, and
the primary user-facing flow(s) once a real feature exists.

## Builds

```bash
npm run build:backend
npm run build:frontend
```

