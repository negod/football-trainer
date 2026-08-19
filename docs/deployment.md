# Deployment

## Backend Runtime Variables

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username. |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password. |
| `APP_AUTH_TOKEN_SECRET` | Secret used to sign bearer tokens. |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins. |
| `PORT` | HTTP port, default `8080`. |

## Frontend Build Variables

| Variable | Description |
|---|---|
| `VITE_API_BASE_URL` | Backend API base URL ending in `/api`. |
| `VITE_AUTH_TOKEN_STORAGE_KEY` | Local storage key for auth token. |
| `VITE_AUTH_USER_STORAGE_KEY` | Local storage key for auth user. |

## Release Notes

- Backend can be deployed as a Spring Boot jar or container.
- Frontend can be deployed as static assets from `frontend/dist`.
- Liquibase runs automatically before Hibernate validates the schema.

