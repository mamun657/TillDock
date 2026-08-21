# TillDock backend

This module is the only component that talks to PostgreSQL/Neon. The Android client NEVER imports or sees this module.

## Required environment

The backend reads configuration from environment variables only. Copy `.env.example` to `.env` and fill in real values, then export them before running.

```
DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>?sslmode=require
JWT_SECRET=<replace-with-a-strong-random-secret-of-at-least-32-chars>
SERVER_PORT=8080
JWT_EXPIRATION_MINUTES=1440
BCRYPT_STRENGTH=12
```

`DATABASE_URL` is the only place the PostgreSQL connection string lives. The Android client never receives it.

## Migrate

The project uses Flyway. The migration `V1__create_merchants.sql` is applied automatically on startup. No manual SQL is required.

To run migrations manually with the Maven plugin (optional):

```
mvn -DskipTests package
java -jar target/tilldock-auth-1.0.0.jar
```

## Run

```
mvn -DskipTests spring-boot:run
```

The API listens on port 8080 by default.

## Endpoints

| Method | Path               | Auth     | Purpose                                 |
|--------|--------------------|----------|-----------------------------------------|
| POST   | /api/auth/signup   | public   | Create a merchant and return a token    |
| POST   | /api/auth/login    | public   | Authenticate and return a token         |
| POST   | /api/auth/logout   | bearer   | Acknowledge logout (client clears state)|
| GET    | /api/auth/me       | bearer   | Return the authenticated merchant       |
| GET    | /health            | public   | Liveness probe                          |

## Rotating the leaked Neon credential

The connection string shared in development is treated as compromised. Rotate it in Neon before production and update `DATABASE_URL` only on the backend host.