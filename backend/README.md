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
- H2 (in-memory)
- JJWT

## Quick Start

```bash
cd /Users/talshor/git/mondial/backend
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
