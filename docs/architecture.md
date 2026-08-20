# Architecture

The backend follows Clean Architecture.

```text
infrastructure -> application -> domain
```

## Layers

- `domain`: framework-free business model, invariants, services, and repository ports.
- `application`: use cases, DTOs, mappers, transaction boundaries, and authorization decisions.
- `infrastructure`: Spring MVC controllers, JPA entities, repository adapters, security filters, config.
- `shared`: framework-free exceptions and small shared primitives.

## Frontend Structure

- `src/app`: app shell and routing.
- `src/pages`: route-level screens.
- `src/features/<feature>/api`: feature API clients.
- `src/features/<feature>/components`: feature UI.
- `src/shared`: shared API client, auth context, hooks, types, and generic components.

## Initial Vertical Slice

The template includes only health endpoints and shared infrastructure. The first real feature should add a small end-to-end workflow with:

- domain model
- use case
- persistence
- REST controller
- frontend API
- route page
- tests

## Fair Team Splitting (domain)

`se.backede.coachhub.domain.model` / `domain.service` (issue #106, part of
epic #96) hold the pure algorithm for splitting the players present at a
session into balanced teams:

- `PlayerId`: a validated player identifier, kept independent of the not-yet-built
  `Player` entity (epic #1) so the algorithm has no build-order dependency on it.
- `TeamAssignment`: a full split — a list of teams, each a list of `PlayerId` —
  used both as the algorithm's output and as the shape of a previously saved
  assignment fed back in as history.
- `PairingHistory`: derived from a list of past `TeamAssignment`s, answers how
  many times two players have already shared a team this season.
- `TeamSplitService`: given the present players, a team count and a
  `PairingHistory`, produces a `TeamAssignment` with teams whose sizes differ
  by at most one, using a greedy heuristic (process players in a deterministic
  order, place each into the non-full team with the lowest summed pairing
  history against its current members). Not an optimal solution — good enough
  for the squad sizes involved, and easy to reason about and test.

Persistence (`team_assignment` / `team_assignment_member` tables), the REST
API and wiring to real `Player`/`Session` records are added in #107; the
frontend in #108.

## Team & Roster Foundation (domain)

`Team` (issue #32, feature #6, epic #1) is the first piece of the real
ownership boundary:

- `CoachId`: the authenticated coach identity every private resource is
  scoped to. Coach accounts (epic #20) don't exist yet, so this is a
  standalone value type for now; once #20 lands, its id fills this role and
  the infrastructure layer resolves it from the security context instead of
  a caller-supplied value.
- `Team`: name, birth year, gender category (`GenderCategory`:
  boys/girls/mixed). Validates a non-blank name (max 100 chars) and a
  plausible, non-future birth year. `Team.shorthand()` derives the Swedish
  convention (e.g. "P19") from gender + birth year. Match format is
  intentionally not stored here (see feature #6) — it will live on `Period`
  (epic #2) instead, since it changes as the cohort ages.
- `TeamRepositoryPort` + `TeamUseCaseService`: CRUD scoped to the owning
  coach — `AccessDeniedException` (new in `shared/exception`) when a
  requester doesn't own the team, `ResourceNotFoundException` when it
  doesn't exist.

### Persistence and API (issue #33)

- Liquibase `0001_create_team.xml`: `team` table (`id`, `owner_id`, `name`,
  `birth_year`, `gender_category`) with an index on `owner_id`.
- `TeamEntity` / `SpringDataTeamRepository` / `JpaTeamRepositoryAdapter`
  implement `TeamRepositoryPort` — the domain never sees the entity.
- `TeamController`: `POST /api/teams`, `GET /api/teams`,
  `GET /api/teams/{id}`, `PATCH /api/teams/{id}`. Ownership is never taken
  from the request; the controller asks `CurrentCoachResolver` for it.
- **Interim single-tenant auth seam.** `CurrentCoachResolver` is an
  infrastructure port; its only implementation right now,
  `SingleTenantCurrentCoachResolver`, always returns the same fixed
  `CoachId` (`00000000-0000-0000-0000-000000000001`). `SecurityConfig`
  permits `/api/teams/**` so the app is actually usable before epic #20
  (Coach Accounts & Authentication) exists — every subsequent controller
  under the same ownership boundary (players, sessions, team-split) should
  follow the same pattern. This is a deliberate, tracked deviation from
  feature #6/#7's literal "authenticated coach" wording, agreed as an
  interim step; **epic #20 must delete `SingleTenantCurrentCoachResolver`,
  add a real implementation backed by `SecurityContextHolder`, and lock
  `SecurityConfig` back down to `.authenticated()`** — nothing else needs to
  change, since every use case already takes an explicit `CoachId` and
  enforces ownership with it.

### Frontend (issue #34)

- `frontend/src/features/teams/api/teamsApi.ts`: typed `listTeams` /
  `createTeam` / `updateTeam` calling `/teams` through the shared
  `apiRequest` client.
- `frontend/src/features/teams/components`: `TeamForm` (create/edit, no
  direct fetch calls) and `TeamList` (loading/empty/error handled by the
  page, not the component).
- `frontend/src/pages/TeamsPage.tsx`: the first route page — fetches the
  list via `useAsync`, and switches `TeamForm` between create and edit mode.
  `App.tsx`/`main.tsx` now wire up `react-router-dom` for the first time.

