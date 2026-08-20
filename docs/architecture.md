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

