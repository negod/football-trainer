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

- `PlayerId`: a validated player identifier. Originally kept independent of
  the not-yet-built `Player` entity so the algorithm had no build-order
  dependency on it; now that `Player` exists (issue #35), it reuses this
  same type rather than introducing a second id.
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

## Player Roster (domain)

`Player` (issue #35, feature #7, epic #1) models a team's roster. Domain and
use cases only — no persistence, controller or frontend in this issue (those
are #36 and #37).

- `Player`: `teamId` (a `TeamId`), name, birth year, optional `position`.
  Validates a non-blank name (max 100 chars), a plausible non-future birth
  year (same rule as `Team`), and — if given — a non-blank `position` up to
  50 chars; a blank position is normalized to `null` rather than rejected,
  since the field is optional free text (not an enum — terminology varies by
  club and age group). `Player.belongsToTeam(TeamId)` backs the ownership
  check below.
- `PlayerId`: the identifier introduced for Fair Team Splitting (#106) is
  reused as-is rather than duplicated — see "Fair Team Splitting" above.
  `PlayerId.newId()` was added for `Player.create(...)` to mint one.
- `PlayerRepositoryPort` + `PlayerUseCaseService`: CRUD scoped through the
  *team's* owning coach, since a player has no owner of its own. Every
  operation first loads the team via `TeamRepositoryPort` and throws
  `AccessDeniedException` if the requester doesn't own it (`ResourceNotFoundException`
  if the team itself doesn't exist); a player found by id that belongs to a
  *different* team is also reported as `ResourceNotFoundException` rather
  than `AccessDeniedException`, so a coach can't use another team's player
  ids to probe for existence.
- Deletion's effect on future references (the feature's "handled explicitly
  rather than silently broken" acceptance criterion) is moot for now since
  nothing yet references a `Player` by id; revisit once Session/attendance
  (epic #2) exists.

### Persistence and API (issue #36)

- Liquibase `0002_create_player.xml`: `player` table (`id` — `varchar(36)`,
  since `PlayerId` is a string, not a `TeamId`-style UUID — `team_id`, `name`,
  `birth_year`, `position`) with a `fk_player_team` foreign key to `team(id)`
  and an index on `team_id`.
- `PlayerEntity` / `SpringDataPlayerRepository` / `JpaPlayerRepositoryAdapter`
  implement `PlayerRepositoryPort` the same way `Team`'s adapter does.
  `PlayerUseCaseService` now carries `@Service` (see #35's note, now resolved
  — the port has an adapter to autowire).
- `PlayerController`: `POST/GET /api/teams/{teamId}/players`,
  `GET/PATCH/DELETE /api/teams/{teamId}/players/{id}`. Ownership is resolved
  the same way as `TeamController`, through `CurrentCoachResolver`; the path
  additionally carries `teamId` since a player has no owner of its own.
- **No `SecurityConfig` change.** The existing `.requestMatchers("/api/teams/**")`
  permit-list entry already covers the nested player routes — Spring's `**`
  segment matches across path segments, not just the one immediately after
  `/api/teams/`. `PlayerSecurityConfigTest` runs with the real filter chain
  (unlike the other controller tests, which disable it) to confirm this
  rather than assume it, so no separate `/api/teams/*/players/**` entry was
  added.

### Frontend (issue #37)

- `frontend/src/features/players/api/playersApi.ts`: typed `listPlayers` /
  `createPlayer` / `updatePlayer` / `deletePlayer` calling
  `/teams/{teamId}/players` through the shared `apiRequest` client.
  `teamsApi.ts` gained `getTeam(id)` (the backend endpoint already existed;
  the frontend just hadn't needed it yet) to show the roster page's heading.
- `frontend/src/features/players/components`: `PlayerForm` (create/edit,
  position is a plain optional text input, submitted as `null` when blank)
  and `PlayerList` (Edit + Remove per row — unlike `TeamList`, which has no
  delete action since #34 never wired one up; removal is one of feature #7's
  acceptance criteria, so `PlayerList` needed it from the start).
- `frontend/src/pages/PlayersPage.tsx`: route `/teams/:teamId/players`
  (added to `App.tsx`). Loads the team (for the heading) and the roster
  independently via `useAsync`; create/edit reload the list the same way
  `TeamsPage` does, and remove has its own error handling since it isn't
  behind a form.
- `TeamList` gained a "Roster" link to `/teams/{id}/players` — the only entry
  point into the roster UI, so it was a required (not incidental) change
  here despite touching #34's component.
- **Bug fixed in passing:** `WebConfig`'s CORS `allowedMethods` was missing
  `PATCH`, so the browser's preflight for every `PATCH` request (edit, for
  both `Team` and `Player`) was rejected with 403 before reaching the
  controller — invisible in existing tests since none exercise real
  browser CORS. Found via manual browser verification of the edit flow,
  fixed as a one-line addition since it directly blocked this issue's
  "works end to end" completion criterion.

## Season & Schedule Planning (domain)

`Period` (issue #38, feature #8, epic #2) models a team's season/term.
Domain and use cases only in #38 — no persistence, controller or frontend
(those are #39 and #40).

- `MatchFormat`: the 3v3/5v5/7v7/9v9/11v11 tier enum, deliberately its own
  top-level domain type (not nested in `Period`) since it's shared with
  `Drill.level` (epic #5, issue #16) and will outlive any one owner.
  `MatchFormat.suggestedFor(birthYear)` implements SvFF's national
  age-to-format guideline as a pure function of the current year and a
  team's birth year — a suggestion only, always overridable; ties at the
  9v9/11v11 boundary (age 15 falls in both tiers per the guideline) resolve
  to 9v9.
- `Period`: `teamId`, name, `startDate`/`endDate`, `format`. Validates a
  non-blank name (max 100 chars, same rule as `Team`/`Player`) and that
  `endDate` is strictly after `startDate`. Format is required and, unlike
  `Team.genderCategory`, deliberately **not** inherited automatically from
  `Team` — see feature #8's rationale (a cohort's playable format changes as
  it ages, independent of the team record itself).
- `PeriodId`: a new `UUID`-backed id (like `TeamId`), not the `String`-backed
  `PlayerId` — that type's string-ness came from the pre-existing Fair Team
  Splitting algorithm's design, not a general convention to follow.
- `PeriodRepositoryPort` + `PeriodUseCaseService`: CRUD scoped through the
  *team's* owning coach, same ownership pattern as `Player`. No `deleteById`
  on the port/use case — feature #8's API contract only lists
  `POST/GET/PATCH /teams/{teamId}/periods`, no `DELETE` (periods will anchor
  generated `Session`s in #9, so removal needs its own explicit design later
  rather than a bare delete now). `suggestFormat(requester, teamId)` loads
  the team (enforcing the same ownership check) and returns
  `MatchFormat.suggestedFor(team.birthYear())`; #40 decides how the frontend
  surfaces this — reusing the use case avoids duplicating the age-to-format
  table client-side.
- Not yet a Spring bean, same reason and same resolution timing as
  `PlayerUseCaseService` in #35/#36: `PeriodRepositoryPort` has no adapter
  until #39.

### Persistence and API (issue #39)

- Liquibase `0003_create_period.xml`: `period` table (`id`, `team_id`, `name`,
  `start_date`, `end_date`, `format`) with an `fk_period_team` foreign key to
  `team(id)` and an index on `team_id`.
- `PeriodEntity` / `SpringDataPeriodRepository` / `JpaPeriodRepositoryAdapter`
  implement `PeriodRepositoryPort`, same pattern as `Team`/`Player`.
  `PeriodUseCaseService` is `@Service` again now that the adapter exists.
- `PeriodController`: `POST/GET /api/teams/{teamId}/periods`,
  `GET/PATCH /api/teams/{teamId}/periods/{id}` — matches feature #8's
  contract exactly (no `DELETE`, per #38's note). Also adds
  `GET /api/teams/{teamId}/periods/suggested-format`, not explicitly listed
  in the feature card but needed to satisfy its "a default format is
  suggested" acceptance criterion without duplicating
  `MatchFormat.suggestedFor`'s age-to-format table in TypeScript; #38 already
  built and tested `PeriodUseCaseService.suggestFormat` for exactly this.
  Placed as a literal `/suggested-format` segment ahead of the `/{id}`
  mapping — Spring's path matching ranks literal segments over path
  variables, so this doesn't collide with period-by-id lookups (the same
  pattern REST APIs commonly use, e.g. `/users/me` vs. `/users/{id}`).
- No `SecurityConfig` change, same reasoning as #36 for players: the
  existing `/api/teams/**` permit-list entry already covers
  `/api/teams/{teamId}/periods/**`; already verified with a real filter
  chain by `PlayerSecurityConfigTest`, so not re-verified per resource.

### Frontend (issue #40)

- `frontend/src/features/periods/api/periodsApi.ts`: typed `listPeriods` /
  `createPeriod` / `updatePeriod` / `getSuggestedFormat` against
  `/teams/{teamId}/periods`. No `deletePeriod` — matches #39's contract
  (periods have no delete endpoint yet).
- `frontend/src/features/periods/matchFormat.ts`: the `MatchFormat` →
  display-label map (`3v3`…`11v11`) and the ordered list used to populate
  the format `<select>`. Pulled out of `PeriodForm` into its own module
  (rather than exported alongside the component) purely to satisfy the
  `react-refresh/only-export-components` lint rule; `PeriodList` needs the
  same labels for its list rows.
- `PeriodForm`: on create, defaults `format` to the `suggestedFormat` prop
  (fetched via `getSuggestedFormat`) with a note under the label that it's a
  suggestion and stays overridable — the acceptance criterion in feature #8.
  On edit, the period's own stored format is used instead, never the
  suggestion. `PeriodsPage` keys the form on
  `` `create-${suggestedFormat ?? 'pending'}` `` so it remounts (and picks
  up the real default) once the suggestion finishes loading, since the
  suggestion arrives asynchronously after the form's first render.
- `PeriodList`: no delete/remove action, unlike `PlayerList` — periods have
  no delete endpoint (see #39).
- `frontend/src/pages/PeriodsPage.tsx`: route `/teams/:teamId/periods`
  (added to `App.tsx`). Loads the team, the period list and the suggested
  format independently via `useAsync`; create/edit reload the list the same
  way `PlayersPage` does.
- `TeamList` gained a "Periods" link (alongside the existing "Roster" link)
  to `/teams/{id}/periods` — the entry point into this flow.
- Manually verified in a real browser against an isolated dev stack
  (Postgres on 5433, backend on 8081, frontend on 5174 — reserved on the
  issue, torn down after): created a team (birth year 2015 → age 11),
  confirmed the periods page suggested 7v7 by default, created a period,
  edited its format to 9v9, and checked the 320px layout. No CORS issues
  this time — #37 already fixed `WebConfig`'s missing `PATCH`.
