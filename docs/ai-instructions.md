# AI Coding Assistant Instructions

This is the canonical instruction file for AI assistants working in this
repository. The root files `AGENTS.md` (Codex/ChatGPT) and `CLAUDE.md`
(Claude) are short entry points that point here. Change shared rules in this
file first so the assistants' instructions don't drift apart.

## Mandatory worktree check before any file change

**No AI assistant may change files, run write-mode formatting, stage, or
commit in the main checkout (`main`).** The main checkout is a stable
integration/demo environment only. All change work happens in its own Git
worktree and branch per [`work-items.md`](work-items.md), even when the work
is small or has no GitHub issue yet.

This rule applies no matter how the request is phrased. Words like
"continue", "fix", "update", "implement", "refactor", "document" are all
change work. It covers code, tests, documentation, configuration, scripts
and generated files. A direct request in a conversation counts as a Task
even if the user doesn't give a task number.

Before the first file change, the assistant must always:

1. run `git status --short`, `git branch --show-current` and
   `git worktree list`;
2. determine whether the current directory is the main checkout or another
   agent's worktree;
3. if it's the main checkout: **stop all change work there** and create or
   resume its own worktree and branch;
4. change the working directory to its own worktree and verify status and
   branch again;
5. only then change files or run tools that can write to the repo.

Uncommitted changes in the main checkout are not permission to keep working
there, even if another agent or an earlier session created them. Don't
change, move, stage, commit, or revert them. Instead create a clean worktree
from an updated `origin/main`. If the task genuinely depends on the
uncommitted changes, stop and ask the user how to handle them.

Read-only work — reading files, checking status, diagnosing — is fine in the
main checkout. As soon as the task needs a file change, the worktree
requirement above applies without exception based on the prompt's wording or
the task's size.

### The main checkout after a merge

As soon as an agent sees that its own or another agent's PR has merged to
`main`, the main checkout's local `main` branch should be updated before the
next work item: `git pull --ff-only origin main`. This doesn't count as
change work — it's a pure fast-forward that keeps the main checkout's `main`
identical to `origin/main`, on par with the read-only commands above. Only
run it when `git status --short` prints nothing; if `--ff-only` fails,
there are local commits or changes in the main checkout that don't belong
there — stop and ask the user instead of forcing it.

## Core principles

- Use Clean Architecture with dependencies pointing inward.
- Keep the domain free of framework, ORM, servlet and database code.
- Model the project's business rules explicitly, with ownership boundaries
  named in the domain.
- Follow SOLID and prefer simple, readable code over speculative
  abstractions.
- Use TDD for new behavior.
- Treat private/user data as sensitive by default.
- Document behavior, schema, API and operations in the same change as the
  code.

## Token and session discipline

The goal is good results at the lowest reasonable token cost, without
compromising security, tests or the Definition of Done.

- Default to a balanced model at a low reasoning level; raise it only when
  the task needs real debugging, design judgment, or several dependent
  steps.
- Reserve a flagship model or high/higher reasoning for clearly hard
  problems: security-critical changes, complex architecture, unclear
  regressions, or when a simpler run already fell short.
- Model and reasoning level are chosen by the user when a session starts
  (CLI flag, client setting, or similar) — a running session can't switch
  models by reading this file. The checkpoint rule below is therefore the
  actual mechanism for getting the right model for the next chunk of work: a
  new thread gives the user the chance to reconsider.
- Keep interim messages short and result-driven. Report decisions,
  verification, blockers and next steps; avoid re-narrating work already
  done.
- Keep terminal and tool output limited to the relevant part. Search and
  read with intent, and never expose secrets or large local private data in
  output.
- Reuse context already verified; don't rerun tests, builds, searches or
  reads without a concrete reason such as an affected change or an updated
  branch.
- A natural handoff point is concrete, not a feeling: every time a Task, Bug
  or Feature issue is merged and closed, the agent has reached one. At that
  point, leave a compact checkpoint — active issue, branch/worktree, last
  verification, open review findings and the next concrete step — and
  actively recommend a new thread for the next independent Task/Bug, instead
  of continuing straight into the next issue in the same thread. Continue in
  the same thread only when the user explicitly asks after the
  recommendation, or when the next step is a direct follow-up on the same
  issue (e.g. addressing a review finding on a PR that's still open).
- Don't repeat standing instructions in prompts when they already live in
  this file. New rules should be short, specific, and added at the right
  persistent level.

## Project status

Describe here, in a couple of sentences updated as the project evolves, what
already exists and what's still a stub or missing. Don't claim planned
features exist. Update this section when they're introduced.

## Architecture rules

- Dependencies: `infrastructure -> application -> domain`.
- Controllers must not contain business logic or call repositories directly.
- Persistence entities must not be returned from use cases or controllers.
- Use DTOs for HTTP and mappers between DTO, domain and persistence.
- Authorization happens in use cases, not only in the web layer.
- Every private resource is tied to an explicit ownership boundary
  (tenant/household/account/user — name the concept for this project).
- A client-supplied ownership id must never by itself grant access to data.
- Reusable frontend components must not make direct `fetch` calls.

## Placement

| Code | Path |
|---|---|
| Domain models | `backend/src/main/java/<package>/domain/model` |
| Repository ports | `backend/src/main/java/<package>/domain/repository` |
| Use cases | `backend/src/main/java/<package>/application/usecase` |
| DTOs and mappers | `backend/src/main/java/<package>/application/dto` and `mapper` |
| Controllers | `backend/src/main/java/<package>/infrastructure/web` |
| JPA | `backend/src/main/java/<package>/infrastructure/persistence` |
| Frontend features | `frontend/src/features/<feature>` |
| Route pages | `frontend/src/pages` |
| Shared frontend code | `frontend/src/shared` |

## Naming

- Domain model: `Item`
- Repository port: `ItemRepositoryPort`
- Use case service: `ItemUseCaseService`
- Request: `CreateItemRequest`, `UpdateItemRequest`
- Response: `ItemResponse`
- JPA entity: `ItemEntity`
- Adapter: `JpaItemRepositoryAdapter`
- Spring Data: `SpringDataItemRepository`

## Feature workflow

1. Describe the rules and the security boundary.
2. Write domain tests and the domain model.
3. Add the repository port, DTOs, mapper and use case.
4. Test the use case with mocked ports, including denied access across the
   ownership boundary.
5. Add the persistence adapter and migration changeset.
6. Test the controller, validation and persistence where useful.
7. Add typed API functions and components in the right frontend feature.
8. Test loading, empty, error, success and keyboard flow.
9. Run the full test suite and both production builds.
10. Update relevant docs before considering the feature done.

## Epic, feature, task and bug

Use the work model and templates in [`work-items.md`](work-items.md) and
`.github/ISSUE_TEMPLATE`. When a request is made directly in a conversation,
use the same fields and Definition of Done as the work contract, even if no
GitHub issue is created.

- An epic is planned and broken down; the whole epic is only implemented on
  explicit request.
- A feature is delivered as a verified vertical user flow.
- A task is executed within its explicit scope.
- A bug is reproduced before the fix and gets a regression test when
  practical.
- Make reasonable, reversible assumptions and state them. Ask only when a
  missing decision materially changes behavior, security, data or delivery
  scope.
- For feature F-<n> and epic E-<n>, the full flow in `work-items.md` also
  applies: analyze, break down into their own Task/Bug issues, plan,
  execute them one at a time, test, commit, push and open a PR. Keep issues
  small and independent, check that an issue isn't already taken, and
  follow the worktree, runtime and commit rules in `work-items.md`.
- The GitHub Project is the canonical status picture. Update and verify the
  status of the active Task/Bug issue and, where relevant, its parent
  Feature/Epic when work starts, goes to review, is blocked, resumes or
  finishes. A comment or open PR never substitutes for project status.
- Before taking a new Task/Bug, run the review gate in `work-items.md`:
  check your own open PRs for new or unresolved Copilot comments and handle
  them first, unless the user has explicitly prioritized otherwise or the
  action is blocked.

## Security

- Validate all input at system boundaries and protect invariants in the
  domain.
- Never log credentials, tokens, or private/sensitive data.
- Use an ORM or parameterized queries.
- Use least privilege for users, database and hosting.
- Require explicit ownership/membership for every private use case.
- Keep the health endpoint minimal.
- Never commit real secrets or `.env` files to Git.
- Flag known CVEs and block production on unknown critical/high risks.
- Document the threat model when a new kind of private data or integration
  is introduced.

## Database migrations

- Use Liquibase (or your migration tool) changesets under
  `backend/src/main/resources/db/changelog/changes`.
- Start at `0001` and use the next sequential zero-padded prefix.
- Register every file in `db.changelog-master.yaml`.
- Never edit a changeset that has run in a shared environment.
- Keep `spring.jpa.hibernate.ddl-auto=validate`.
- Verify schema changes against the real database engine, ideally with
  Testcontainers.

## Documentation responsibilities

- Root `README.md`: product, setup, commands and user-visible behavior.
- `docs/architecture.md`: domain, layers, security, API and data flows.
- `docs/testing.md`: strategy, commands and covered behavior.
- `docs/deployment.md`: runtime, configuration, migration and release.
- `docs/github-actions.md`: actual CI/CD once workflows are introduced.

## Forbidden

- No JPA annotations in domain models.
- No framework services injected into domain objects.
- No business logic in controllers.
- No database entities exposed through REST.
- No direct `fetch` in reusable components.
- No persisted private data without authentication and tenant/ownership
  authorization.
- No schema changes via ORM auto-create/update.
- No feature considered done without tests for validation, not-found and
  authorization.
