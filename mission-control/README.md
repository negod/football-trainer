# Football Trainer Mission Control

A zero-dependency Node tool for local dev orchestration: start, stop and
restart Postgres (Docker), the backend and the frontend, run the Playwright
E2E suite, and open the running app in your browser. It's a thin wrapper
around the same commands in the root `package.json` and `docker-compose.yml`
— it adds process lifecycle (background start, PID tracking, health checks)
on top, it doesn't replace them.

Requires Node.js, Docker and Maven already on `PATH`, same as the rest of
this repo. No `npm install` is needed inside this folder.

## Dashboard (recommended)

```bash
npm run mc:ui
```

Opens a browser tab at `http://localhost:4400` with a button per service
(start/restart/stop), a logs panel, and a "Run E2E tests" button. It's a
plain HTTP server (`src/server.mjs` + `src/dashboard.html`, no framework)
that calls the exact same functions as the CLI below (`src/actions.mjs`) —
whichever you use, the underlying behavior is identical. Pass `-p <port>` to
use a different port.

## CLI

Run from the repository root:

```bash
node mission-control/src/cli.mjs <command> [target] [flags]
```

Or via the root scripts (see root `package.json`):

```bash
npm run mc:start
npm run mc:status
npm run mc:e2e
```

## Commands

| Command | Target | Description |
|---|---|---|
| `ui [-p port]` | — | Opens the dashboard in your browser (default port 4400). |
| `start` | `db\|backend\|frontend\|all` (default `all`) | Starts the service(s) and waits for a health check. Starting `backend` alone also starts `db` first. |
| `stop` | `db\|backend\|frontend\|all` (default `all`) | Stops the service(s), in reverse dependency order for `all`. |
| `restart` | same as above | Stop then start. |
| `status` | — | Table of every service's state, PID and port. Add `-w`/`--watch` to refresh it every 2s. |
| `logs <service>` | `db\|backend\|frontend` | Prints the service's log. Add `-f`/`--follow` to keep streaming. |
| `e2e` | — | Ensures every service is running and healthy, then runs `npm run test:e2e` in `frontend/` (Playwright). |
| `open [frontend\|backend]` | default `frontend` | Opens the URL in your default browser. |

Add `--open` to `start`/`restart` to open the frontend in your browser once
it reports healthy:

```bash
node mission-control/src/cli.mjs start all --open
```

## Hot reload

- Frontend: Vite's dev server (already started by `start frontend`) hot
  module reloads on save.
- Backend: `spring-boot-devtools` (see `backend/pom.xml`) restarts the
  Spring context automatically once `target/classes` changes — recompile
  from your IDE or run `mvn -f backend compile` in another terminal while
  `mission-control` keeps the process running.

## State

Runtime PID files and logs live under `mission-control/.state/` (git-ignored,
created on first `start`). Deleting that folder while services are running
will orphan them — stop everything first.

## Health checks

- `backend`: `GET http://localhost:8080/api/health`
- `frontend`: `GET http://localhost:5173/`
- `db`: the container's Docker healthcheck (`pg_isready`, defined in
  `docker-compose.yml`) must report `healthy`.
