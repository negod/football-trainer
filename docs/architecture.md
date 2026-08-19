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

