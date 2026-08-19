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

## Builds

```bash
npm run build:backend
npm run build:frontend
```

