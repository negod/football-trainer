# AI Project Template

This repository is a full-stack starter intended to be copied into a new project and completed by an AI coding assistant or a developer.

The stack is:

- Spring Boot 3, Java 21, Maven
- PostgreSQL
- Liquibase
- React 18, Vite, TypeScript
- Tailwind CSS
- Vitest and React Testing Library
- Playwright for E2E tests
- [`mission-control/`](mission-control/README.md): a dashboard (and CLI) to
  start, stop and restart Postgres/backend/frontend, run the E2E suite, and
  open the app in your browser

It also ships the process that lets **Claude and ChatGPT (Codex) work the
same repository in parallel** without stepping on each other: an
Epic/Feature/Task/Bug issue workflow, a GitHub Project as the shared status
board, isolated Git worktrees per issue, and a Copilot review gate with
auto-merge. See [`docs/work-items.md`](docs/work-items.md) for the full
model and [`docs/ai-instructions.md`](docs/ai-instructions.md) for the
engineering rules every assistant reads first.

## First Setup

1. Rename project placeholders:

```text
Coach Hub        # display name, for example Customer Portal
coach-hub        # lowercase slug, for example customer-portal
se.backede.coachhub        # Java package, for example se.backede.customerportal
se/backede/coachhub   # Java path, for example se/backede/customerportal
coach-hub             # database name, for example customer-portal
```

Or run the bootstrap helper from the copied repository root:

```bash
scripts/bootstrap-template.sh \
  --project-name "Customer Portal" \
  --project-slug customer-portal \
  --java-package se.backede.customerportal \
  --db-name customer-portal
```

2. Confirm no placeholders remain:

```bash
rg '__(PROJECT|JAVA|DB)_' --glob '!README.md'
```

3. Install frontend dependencies:

```bash
npm install --prefix frontend
```

4. Open the mission control dashboard - buttons to start/stop/restart
   Postgres, the backend and the frontend, view logs, and run E2E tests:

```bash
npm run mc:ui
```

   Or start everything from the terminal and open the app in your browser
   once it's ready:

```bash
npm run mc:start -- --open
```

   Or start each service individually:

```bash
npm run dev:db
npm run dev:backend
npm run dev:frontend
```

5. (Optional, for E2E tests) Install Playwright's browser:

```bash
npx --prefix frontend playwright install --with-deps chromium
```

## Commands

```bash
npm run dev:db
npm run dev:backend
npm run dev:frontend
npm run test:backend
npm run test:frontend
npm run test:e2e
npm run build:backend
npm run build:frontend

# Dev orchestration (see mission-control/README.md)
npm run mc:ui
npm run mc:start [-- db|backend|frontend] [-- --open]
npm run mc:stop [-- db|backend|frontend]
npm run mc:restart [-- db|backend|frontend]
npm run mc:status
npm run mc -- logs backend -f
npm run mc:e2e
npm run mc:open
```

## Repository Layout

```text
.
|-- AGENTS.md                      # Codex/ChatGPT entry point
|-- CLAUDE.md                      # Claude entry point
|-- PROMPT-INIT.md                 # paste-in prompt to kick off a new project
|-- .github/
|   |-- ISSUE_TEMPLATE/            # Epic, Feature, Task, Bug forms
|   |-- workflows/                 # ci.yml, auto-merge.yml
|   `-- copilot-instructions.md
|-- backend/
|   |-- src/main/java/se/backede/coachhub/
|   |   |-- domain/
|   |   |-- application/
|   |   |-- infrastructure/
|   |   `-- shared/
|   `-- src/main/resources/
|       |-- application.yml
|       |-- application-local.yml
|       `-- db/changelog/
|-- docs/
|   |-- ai-instructions.md         # canonical rules, read by every assistant
|   |-- work-items.md              # Epic/Feature/Task/Bug + worktree + status model
|   |-- github-actions.md
|   |-- architecture.md
|   |-- deployment.md
|   `-- testing.md
|-- frontend/
|   |-- e2e/                        # Playwright E2E specs
|   |-- playwright.config.ts
|   `-- src/
|       |-- app/
|       |-- pages/
|       |-- features/
|       `-- shared/
|-- mission-control/                # dev orchestration dashboard + CLI (start/stop/restart/e2e)
|-- docker-compose.yml
`-- package.json
```

## GitHub setup for parallel agents

Do this once, right after creating the new repository on GitHub, so Claude
and ChatGPT (or two Claude sessions) can work it in parallel from day one:

1. **Labels.** Create the type and agent labels used by `work-items.md`:

   ```bash
   for l in "type: epic" "type: feature" "type: task" "type: bug" \
            "agent: claude" "agent: codex" \
            "status: blocked" "status: waiting"; do
     gh label create "$l" --repo negod/football-trainer 2>/dev/null || true
   done
   ```

2. **GitHub Project.** Create a Project (Board or Table view) and add a
   single-select `Status` field with exactly these six options, in order:
   `Triage`, `Backlog`, `Ready`, `In progress`, `In review`, `Done`. Turn on
   the built-in workflows so new issues are added as `Triage`, closed issues
   and merged PRs move to `Done`, and reopened issues move back to `Triage`.
   This board is the canonical status source described in
   `docs/work-items.md`.

3. **Branch protection / ruleset for `main`.** Require the `Backend` and
   `Frontend` CI checks and require conversation resolution. Enable
   **"Automatically request Copilot code review"** (including on new pushes)
   if your GitHub plan includes Copilot Pro or higher — this is what
   `docs/work-items.md`'s review gate relies on. Do not require a manual
   approving review; Copilot never issues one.

4. **Replace placeholders.** `__OWNER__/__REPO__` appears in
   `.github/ISSUE_TEMPLATE/config.yml` and the command above; the bootstrap
   script in step 1 of "First Setup" does not touch these — update them by
   hand once the repository has a real name and owner.

## AI Bootstrap Prompt

See [`PROMPT-INIT.md`](PROMPT-INIT.md) for the prompt to paste into a new
Claude or ChatGPT (Codex) session after copying the template and doing the
GitHub setup above. It has the assistant ask you for the project goal,
placeholder values, local path, and GitHub owner/repo before it touches
anything — rather than guessing them — and it's designed to be handed to a
second assistant afterwards to start working in parallel.
