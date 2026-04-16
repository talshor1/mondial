# Mondial Backend

Spring Boot backend API with JWT authentication, BCrypt password hashing, roles, and a database-backed user table.

## Features

- `POST /api/auth/register` - register a new user (default role: `USER`)
- `POST /api/auth/login` - login and receive JWT token
- `GET /api/users/me` - return authenticated user profile (`id`, `email`, `role`)
- Password hashing with BCrypt
- Stateless JWT auth
- Roles (`USER`, `ADMIN`) stored in DB

## Tech Stack

- Java 17
- Spring Boot 3.3.x
- Spring Security
- Spring Data JPA
- PostgreSQL (Neon)
- JJWT

## Quick Start

The project is preconfigured with your Neon PostgreSQL defaults in `application.properties`.

```bash
cd /Users/talshor/git/mondial/backend
mvn spring-boot:run
```

Recommended (override credentials via env vars instead of source defaults):

```bash
cd /Users/talshor/git/mondial/backend
export DB_URL="jdbc:postgresql://ep-long-breeze-amljbz2n.c-5.us-east-1.aws.neon.tech/neondb?sslmode=require"
export DB_USER="neondb_owner"
export DB_PASSWORD="<your-password>"
export APP_JWT_SECRET="replace-with-a-long-random-secret-at-least-32-chars"
mvn spring-boot:run
```

## Test

```bash
cd /Users/talshor/git/mondial/backend
mvn test
```

## Example API Calls

Register:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Password123"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"Password123"}'
```

Profile (`/api/users/me`):

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <JWT_TOKEN>"
```
